package com.armedia.acm.plugins.profile.web.api;

import com.armedia.acm.core.exceptions.AcmObjectNotFoundException;
import com.armedia.acm.plugins.profile.dao.UserOrgDao;
import com.armedia.acm.plugins.profile.exception.AcmProfileException;
import com.armedia.acm.plugins.profile.model.ProfileDTO;
import com.armedia.acm.plugins.profile.model.UserOrg;
import com.armedia.acm.plugins.profile.service.ProfileEventPublisher;
import com.armedia.acm.services.users.dao.ldap.UserDao;
import com.armedia.acm.services.users.model.AcmRole;
import com.armedia.acm.services.users.model.AcmUser;
import com.armedia.acm.services.users.model.RoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marjan.stefanoski on 20.10.2014.
 */

@Controller
@RequestMapping({"/api/v1/plugin/profile", "/api/latest/plugin/profile"})
public class GetProfileInfoAPIController {

    private UserDao userDao;
    private UserOrgDao userOrgDao;


    private Logger log = LoggerFactory.getLogger(getClass());
    private ProfileEventPublisher eventPublisher;

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProfileDTO getProfileInfo(
            Authentication authentication,
            HttpSession session
    ) throws AcmProfileException, AcmObjectNotFoundException {
        String userId = (String) authentication.getName().toLowerCase();
        if (log.isInfoEnabled()) {
            log.info("Finding Profile info for user '" + userId + "'");
        }
        AcmUser user = userDao.findByUserId(userId);
        if (user == null) {
            throw new AcmObjectNotFoundException("user",null, "Object not found", null);
        }
        UserOrg userOrg = null;
        ProfileDTO profileDTO = null;
        List<AcmRole> groups = null;
        try {
             userOrg = getUserOrgDao().getUserOrgForUser(user);
        } catch (AcmObjectNotFoundException e){
            if(log.isInfoEnabled()){
                log.info("Profile info for the user: " + userId + "is not found");
            }
            //add only user data like full name, email, userId , groups
            userOrg = new UserOrg();
            userOrg.setUser(user);
        }
        groups = getUserDao().findAllRolesByUserAndRoleType(userId,RoleType.LDAP_GROUP);
        profileDTO = prepareProfileDto(userOrg, groups);
        return profileDTO;
    }

    private ProfileDTO prepareProfileDto(UserOrg userOrgInfo, List<AcmRole> ldapRoles){
        ProfileDTO profileDTO = new ProfileDTO();

        List<String> groups = new ArrayList<>();

        for (AcmRole role: ldapRoles){
            groups.add(role.getRoleName());
        }

        profileDTO.setGroups(groups);

        profileDTO.setEmail(userOrgInfo.getUser().getMail());
        profileDTO.setFullName(userOrgInfo.getUser().getFullName());

        profileDTO.setCity(userOrgInfo.getCity());
        profileDTO.setCompanyName(userOrgInfo.getCompanyName());
        profileDTO.setFax(userOrgInfo.getFax());
        profileDTO.setFirstAddress(userOrgInfo.getFirstAddress());
        profileDTO.setImAccount(userOrgInfo.getImAccount());
        profileDTO.setImSystem(userOrgInfo.getImSystem());
        profileDTO.setLocation(userOrgInfo.getLocation());
        profileDTO.setMainOfficePhone(userOrgInfo.getMainOfficePhone());
        profileDTO.setMobilePhoneNumber(userOrgInfo.getMobilePhoneNumber());
        profileDTO.setOfficePhoneNumber(userOrgInfo.getOfficePhoneNumber());
        profileDTO.setSecondAddress(userOrgInfo.getSecondAddress());
        profileDTO.setState(userOrgInfo.getState());
        profileDTO.setWebsite(userOrgInfo.getWebsite());
        profileDTO.setZip(userOrgInfo.getZip());
        profileDTO.setUserId(userOrgInfo.getUser().getUserId());
        profileDTO.setPictureUrl("");

        return profileDTO;
    }

    public UserOrgDao getUserOrgDao() {
        return userOrgDao;
    }

    public void setUserOrgDao(UserOrgDao userOrgDao) {
        this.userOrgDao = userOrgDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public void setEventPublisher(ProfileEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
