package com.armedia.acm.services.users.service.ldap;

import com.armedia.acm.data.AuditPropertyEntityAdapter;
import com.armedia.acm.services.users.dao.ldap.SpringLdapDao;
import com.armedia.acm.services.users.model.AcmUser;
import com.armedia.acm.services.users.model.LdapGroup;
import com.armedia.acm.services.users.model.ldap.AcmLdapSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sync the user-related database tables with an LDAP directory. To support multiple LDAP configurations, create multiple Spring beans, each
 * with its own ldapSyncConfig.
 * <p/>
 * Both application roles and LDAP groups are synced.
 * <ul>
 * <li>Application roles drive role-based access control, and every deployment has the same application role names regardless of the LDAP
 * group names. The ldapSyncConfig includes a mapping from the logical role name to the physical LDAP group name. For each entry in this
 * mapping, the members of the indicated LDAP group are linked to the indicated logical application role.</li>
 * <li>LDAP groups are also synced, to be available for data access control; this allows users to grant or deny access to specific groups.
 * The groups could be more granular than application roles; for example, all case agents share the same application roles, but different
 * LDAP groups could represent different functional or geographic areas. So granting access at the LDAP group level could be more
 * appropriate - i.e., would restrict access to only those case agents in the appropriate functional or geographic area.</li>
 * </ul>
 */
public class LdapSyncService
{
    private SpringLdapDao ldapDao;
    private LdapSyncDatabaseHelper ldapSyncDatabaseHelper;
    private AcmLdapSyncConfig ldapSyncConfig;
    private String directoryName;
    private boolean syncEnabled = true;
    private AuditPropertyEntityAdapter auditPropertyEntityAdapter;

    private Logger log = LoggerFactory.getLogger(getClass());

    // this method is used by scheduled jobs in Spring beans loaded dynamically from the ACM configuration
    // folder ($HOME/.acm).
    public void ldapSync()
    {
        if (!isSyncEnabled())
        {
            log.debug("Sync is disabled - stopping now.");
            return;
        }

        log.info("Starting sync of directory: {}; ldap URL: {}", getDirectoryName(), getLdapSyncConfig().getLdapUrl());

        getAuditPropertyEntityAdapter().setUserId(getLdapSyncConfig().getAuditUserId());

        // all the ldap work first, then all the database work; because the ldap queries could be very timeconsuming.
        // If we opened up a database transaction, then spend a minute or so querying LDAP, the database transaction
        // could time out. So we run all the LDAP queries first, then do all the database operations all at once.
        LdapTemplate template = getLdapDao().buildLdapTemplate(getLdapSyncConfig());

        List<AcmUser> ldapUsers = ldapDao.findUsersPaged(template, getLdapSyncConfig());

        List<LdapGroup> ldapGroups = ldapDao.findGroupsPaged(template, getLdapSyncConfig());

        log.trace("Users: {}", ldapUsers == null ? " null " : ldapUsers.size());
        log.trace("Groups: {}", ldapGroups == null ? " null " : ldapGroups.size());
        ldapUsers = filterUsersForKnownGroups(ldapUsers, ldapGroups);
        filterUserGroups(ldapUsers, ldapGroups);
        filterParentGroups(ldapGroups);

        Set<String> applicationRoles = new HashSet<>();
        Map<String, String> roleToGroup = getLdapSyncConfig().getRoleToGroupMap();
        applicationRoles.addAll(roleToGroup.keySet());

        Map<String, Set<AcmUser>> usersByLdapGroup = getUsersByLdapGroup(ldapGroups, ldapUsers);
        Map<String, Set<AcmUser>> usersByApplicationRole = getUsersByApplicationRole(usersByLdapGroup);
        Map<String, String> childParentPair = populateGroupParentPairs(ldapGroups);

        // ldap work is done. now for the database work.
        getLdapSyncDatabaseHelper().updateDatabase(getDirectoryName(), applicationRoles, ldapUsers, usersByApplicationRole, usersByLdapGroup,
                childParentPair);
    }

    public Map<String, Set<AcmUser>> getUsersByLdapGroup(List<LdapGroup> ldapGroups, List<AcmUser> ldapUsers)
    {
        Map<String, Set<AcmUser>> usersByLdapGroup = new TreeMap<>();
        Map<String, LdapGroup> nameToGroup = ldapGroups.stream()
                .collect(Collectors.toMap(LdapGroup::getGroupName, Function.identity()));

        for (LdapGroup group : ldapGroups)
        {
            usersByLdapGroup.put(group.getGroupName(), new HashSet<>());
        }

        for (AcmUser user : ldapUsers)
        {
            user.getLdapGroups()
                    .forEach(ldapGroup ->
                    {
                        // Add user to the group
                        log.debug("Add user '{}' to group '{}'", user.getDistinguishedName(), ldapGroup);
                        Set<AcmUser> users = usersByLdapGroup.get(ldapGroup);
                        users.add(user);
                        // Add user to parent groups
                        LdapGroup group = nameToGroup.get(ldapGroup);
                        group.getMemberOfGroups().forEach(nestedGroup ->
                        {
                            Set<AcmUser> nestedGroupUsers = usersByLdapGroup.get(nestedGroup);
                            nestedGroupUsers.add(user);
                        });
                    });
        }
        return usersByLdapGroup;
    }

    /**
     * For each user keep only groups that are relevant i.e. exist in synced LDAP groups
     *
     * @param ldapUsers  LDAP users list
     * @param ldapGroups LDAP groups list
     */
    public void filterUserGroups(List<AcmUser> ldapUsers, List<LdapGroup> ldapGroups)
    {
        Set<String> ldapGroupsNames = ldapGroups.stream()
                .map(LdapGroup::getGroupName)
                .collect(Collectors.toSet());
        for (AcmUser user : ldapUsers)
        {
            Set<String> userGroups = user.getLdapGroups();
            // remove all groups that are not fetched LDAP groups
            userGroups.retainAll(ldapGroupsNames);
            user.setLdapGroups(userGroups);
        }
    }

    /**
     * For each LDAP group keep only relevant parent groups
     *
     * @param ldapGroups All LDAP groups
     */
    public void filterParentGroups(List<LdapGroup> ldapGroups)
    {
        Set<String> ldapGroupNames = ldapGroups.stream()
                .map(LdapGroup::getGroupName)
                .collect(Collectors.toSet());
        for (LdapGroup group : ldapGroups)
        {
            Set<String> groupGroups = group.getMemberOfGroups();
            groupGroups.retainAll(ldapGroupNames);
            group.setMemberOfGroups(groupGroups);
        }
    }

    /**
     * Keep only users that are relevant i.e. users who are member of synced LDAP groups
     *
     * @param ldapUsers  All LDAP users
     * @param ldapGroups All LDAP groups
     */
    public List<AcmUser> filterUsersForKnownGroups(List<AcmUser> ldapUsers, List<LdapGroup> ldapGroups)
    {

        Set<String> ldapGroupNames = ldapGroups.stream()
                .map(LdapGroup::getGroupName)
                .collect(Collectors.toSet());
        List<AcmUser> filteredUsers = new ArrayList<>();
        for (AcmUser user : ldapUsers)
        {
            Set<String> userGroups = user.getLdapGroups();

            // check if ldapGroupNames contains at least one user group
            if (!Collections.disjoint(ldapGroupNames, userGroups))
            {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    public Map<String, String> populateGroupParentPairs(List<LdapGroup> ldapGroups)
    {
        Map<String, String> groupParentPairs = new TreeMap<>();
        for (LdapGroup group : ldapGroups)
        {
            Set<String> groupParents = group.getMemberOfGroups();
            if (!groupParents.isEmpty())
            {
                // find only groups with parent groups and return child-parent group pairs
                groupParents.stream()
                        .forEach(groupParent -> groupParentPairs.put(group.getGroupName(), groupParent));
            }
        }
        return groupParentPairs;
    }

    public Map<String, Set<AcmUser>> getUsersByApplicationRole(Map<String, Set<AcmUser>> usersByLdapGroup)
    {
        Map<String, Set<AcmUser>> usersByApplicationRole = new TreeMap<>();
        Set<String> ldapGroups = usersByLdapGroup.keySet();

        Map<String, String> roleToGroup = getLdapSyncConfig().getRoleToGroupMap();
        Map<String, List<String>> groupToRoleMap = reverseRoleToGroupMap(roleToGroup);

        // for each role in group find all users in that group, than connect users to role
        ldapGroups.stream().filter(group -> groupToRoleMap.containsKey(group)).forEach(group ->
        {
            log.debug("Group '{}' has roles: {} ", group, groupToRoleMap.get(group));
            for (String applicationRole : groupToRoleMap.get(group))
            {
                // for each role in group find all users in that group, than connect users to role
                Set<AcmUser> usersByGroup = usersByApplicationRole.getOrDefault(applicationRole, new HashSet<>());
                Set<AcmUser> users = usersByLdapGroup.get(group);
                log.debug("Add '{}' users to role '{}'", users.size(), applicationRole);
                usersByGroup.addAll(users);
                usersByApplicationRole.put(applicationRole, usersByGroup);
            }
        });

        return usersByApplicationRole;
    }


    protected Map<String, List<String>> reverseRoleToGroupMap(Map<String, String> roleToGroup)
    {

        Map<String, List<String>> groupToRoleMap = new HashMap<>();
        List<String> roles;
        for (Map.Entry<String, String> roleMapEntry : roleToGroup.entrySet())
        {
            String role = roleMapEntry.getKey();
            if (role != null && !role.trim().isEmpty())
            {
                role = role.trim().toUpperCase();
                String groupList = roleMapEntry.getValue();
                if (groupList != null && !groupList.trim().isEmpty())
                {
                    String[] groups = groupList.split(",");
                    for (String group : groups)
                    {
                        group = group.trim();
                        if (!group.isEmpty())
                        {
                            group = group.toUpperCase();
                            if (groupToRoleMap.containsKey(group))
                            {
                                roles = groupToRoleMap.get(group);
                            } else
                            {
                                roles = new ArrayList<>();
                            }
                            roles.add(role);
                            groupToRoleMap.put(group, roles);
                        }

                    }
                }
            }
        }
        return groupToRoleMap;
    }

    /**
     * Try to sync user from LDAP by given username
     *
     * @param username - username of the user
     */
    public void ldapUserSync(String username)
    {
        log.info("Starting sync user [{}] from ldap [{}]", username, getLdapSyncConfig().getLdapUrl());

        getAuditPropertyEntityAdapter().setUserId(getLdapSyncConfig().getAuditUserId());

        LdapTemplate template = getLdapDao().buildLdapTemplate(getLdapSyncConfig());
        AcmUser user = getLdapDao().findUser(username, template, getLdapSyncConfig());

        Map<String, String> roleToGroup = getLdapSyncConfig().getRoleToGroupMap();
        Map<String, List<AcmUser>> usersByApplicationRole = new HashMap<>();
        Map<String, List<AcmUser>> usersByLdapGroup = new HashMap<>();
        Map<String, String> childParentPair = new HashMap<>();
        // FIXME: will rework with new LDAP sync methods
        /*Map<String, List<String>> groupToRoleMap = reverseRoleToGroupMap(roleToGroup);

        List<LdapGroup> groups = getLdapDao().findGroupsForUser(user, template, getLdapSyncConfig());

        if (groups != null && !groups.isEmpty())
        {
            groups.stream().forEach(group -> {
                String groupName = group.getGroupName().toUpperCase();
                addUsersToMap(usersByLdapGroup, groupName, Arrays.asList(user));
                addUsersToApplicationRole(usersByApplicationRole, groupToRoleMap, Arrays.asList(user), groupName);

                // Here we are interested only for populating "childParentPair"
                calculateGroupsSubgroupsAndUsers(getLdapSyncConfig(), template, group, childParentPair, new ArrayList<>(), new ArrayList<>());
            });
        }

        getLdapSyncDatabaseHelper().updateDatabaseForUser(getDirectoryName(), user, usersByApplicationRole, usersByLdapGroup, childParentPair);*/

    }

    public SpringLdapDao getLdapDao()
    {
        return ldapDao;
    }

    public void setLdapDao(SpringLdapDao ldapDao)
    {
        this.ldapDao = ldapDao;
    }

    public LdapSyncDatabaseHelper getLdapSyncDatabaseHelper()
    {
        return ldapSyncDatabaseHelper;
    }

    public void setLdapSyncDatabaseHelper(LdapSyncDatabaseHelper ldapSyncDatabaseHelper)
    {
        this.ldapSyncDatabaseHelper = ldapSyncDatabaseHelper;
    }

    public AcmLdapSyncConfig getLdapSyncConfig()
    {
        return ldapSyncConfig;
    }

    public void setLdapSyncConfig(AcmLdapSyncConfig ldapSyncConfig)
    {
        this.ldapSyncConfig = ldapSyncConfig;
    }

    public String getDirectoryName()
    {
        return directoryName;
    }

    public void setDirectoryName(String directoryName)
    {
        this.directoryName = directoryName;
    }

    public boolean isSyncEnabled()
    {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled)
    {
        this.syncEnabled = syncEnabled;
    }

    public AuditPropertyEntityAdapter getAuditPropertyEntityAdapter()
    {
        return auditPropertyEntityAdapter;
    }

    public void setAuditPropertyEntityAdapter(AuditPropertyEntityAdapter auditPropertyEntityAdapter)
    {
        this.auditPropertyEntityAdapter = auditPropertyEntityAdapter;
    }
}
