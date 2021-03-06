/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.adminauthenticationdatabase;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.user.authentication.AdminAuthentication;
import fr.paris.lutece.portal.business.user.log.UserLog;
import fr.paris.lutece.portal.business.user.log.UserLogHome;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Collection;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import javax.servlet.http.HttpServletRequest;


/**
 * Data authentication module for admin authentication
 */
public class AdminDatabaseAuthentication implements AdminAuthentication
{
    private static final String PROPERTY_MAX_ACCESS_FAILED = "admindatabaseauthentication.access.failures.max";
    private static final String PROPERTY_INTERVAL_MINUTES = "admindatabaseauthentication.access.failures.interval.minutes";
    private static final String PROPERTY_SERVICE_NAME = "admindatabaseauthentication.auth.service.name";
    private static final String PROPERTY_LOGIN_PAGE_URL = "admindatabaseauthentication.login.page.url";
    private AdminDatabaseUserDAO _dao = new AdminDatabaseUserDAO(  );

    /**
     *
     */
    public AdminDatabaseAuthentication(  )
    {
        super(  );
    }

    /**
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getAuthServiceName()
     */
    public String getAuthServiceName(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_SERVICE_NAME );
    }

    /**
     * @return {@link javax.servlet.http.HttpServletRequest#BASIC_AUTH}
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getAuthType(javax.servlet.http.HttpServletRequest)
     */
    public String getAuthType( HttpServletRequest request )
    {
        return HttpServletRequest.BASIC_AUTH;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#login(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    public AdminUser login( String strAccessCode, String strUserPassword, HttpServletRequest request )
        throws LoginException
    {
        // Creating a record of connections log
        UserLog userLog = new UserLog(  );
        userLog.setAccessCode( strAccessCode );
        userLog.setIpAddress( request.getRemoteAddr(  ) );
        userLog.setDateLogin( new java.sql.Timestamp( new java.util.Date(  ).getTime(  ) ) );

        // Test the number of errors during an interval of minutes
        int nMaxFailed = AppPropertiesService.getPropertyInt( PROPERTY_MAX_ACCESS_FAILED, 3 );
        int nIntervalMinutes = AppPropertiesService.getPropertyInt( PROPERTY_INTERVAL_MINUTES, 10 );
        int nNbFailed = UserLogHome.getLoginErrors( userLog, nIntervalMinutes );

        if ( nNbFailed > nMaxFailed )
        {
            throw new FailedLoginException(  );
        }

        int nUserCode = _dao.checkPassword( strAccessCode, strUserPassword );

        if ( nUserCode != AdminDatabaseUserDAO.USER_OK )
        {
            throw new FailedLoginException(  );
        }

        AdminUser user = _dao.load( strAccessCode, this );

        return user;
    }

    /**
     * For non-external authentication : nothing to do
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#logout(fr.paris.lutece.portal.business.user.authentication.AdminUser)
     */
    public void logout( AdminUser user )
    {
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getAnonymousUser()
     */
    public AdminUser getAnonymousUser(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#isExternalAuthentication()
     * @return false always
     */
    public boolean isExternalAuthentication(  )
    {
        return false;
    }

    /**
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getHttpAuthenticatedUser(javax.servlet.http.HttpServletRequest)
     * @return null always
     */
    public AdminUser getHttpAuthenticatedUser( HttpServletRequest request )
    {
        return null;
    }

    /**
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getLoginPageUrl()
     */
    public String getLoginPageUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_LOGIN_PAGE_URL );
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getNewAccountPageUrl()
     */
    public String getChangePasswordPageUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getDoLoginUrl()
     */
    public String getDoLoginUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getDoLogoutUrl()
     */
    public String getDoLogoutUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getNewAccountPageUrl()
     */
    public String getNewAccountPageUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getViewAccountPageUrl()
     */
    public String getViewAccountPageUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getLostPasswordPageUrl()
     */
    public String getLostPasswordPageUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getLostPasswordPageUrl()
     */
    public String getLostLoginPageUrl(  )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getUserList()
     */
    public Collection getUserList( String strLastName, String strFirstName, String strEmail )
    {
        return _dao.selectAllDatabaseUsers( strLastName, strFirstName, strEmail, this );
    }

    /**
     * @see fr.paris.lutece.portal.business.user.authentication.AdminAuthentication#getUserPublicData(java.lang.String)
     */
    public AdminUser getUserPublicData( String strLogin )
    {
        return _dao.selectUserPublicData( strLogin, this );
    }
}
