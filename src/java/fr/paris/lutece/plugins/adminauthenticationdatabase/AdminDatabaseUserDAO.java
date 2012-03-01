/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;


/**
 * This class provides Data Access methods for AdminDatabaseUser objects
 */
public class AdminDatabaseUserDAO
{
    public static final int USER_NOTFOUND = -1;
    public static final int INVALID_PASSWORD = -2;
    public static final int USER_OK = 0;
    public static final String SQL_QUERY_CHECK_PASSWORD = "SELECT password FROM admin_auth_db_module WHERE  access_code = ? ";
    public static final String SQL_QUERY_LOAD_USER = " SELECT last_name, first_name, email FROM admin_auth_db_module WHERE access_code = ? ";
    private static final String SQL_QUERY_SELECT_ALL_DATABASE_USERS = "SELECT access_code, last_name, first_name, email * FROM admin_auth_db_module";

    /** This class implements the Singleton design pattern. */
    private static AdminDatabaseUserDAO _dao = new AdminDatabaseUserDAO(  );

    /**
     * Returns the unique instance of the singleton.
     *
     * @return the instance
     */
    static AdminDatabaseUserDAO getInstance(  )
    {
        return _dao;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //Access methods to data

    /**
     * Check the password of a given user into the table provided by the database authentication module
     * @param strAccessCode The name of the user
     * @param strPassword the user password
     * @return the the error number
     */
    public int checkPassword( String strAccessCode, String strPassword )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_PASSWORD );
        daoUtil.setString( 1, strAccessCode );
        daoUtil.executeQuery(  );

        if ( !daoUtil.next(  ) )
        {
            daoUtil.free(  );

            return USER_NOTFOUND;
        }

        String strStoredPassword = daoUtil.getString( 1 );
        daoUtil.free(  );

        if ( !strStoredPassword.equals( strPassword ) )
        {
            daoUtil.free(  );

            return INVALID_PASSWORD;
        }

        return USER_OK;
    }

    /**
     * load the data of an user from the table provided by the database authentication module
     * This only provides data specific to the database authentication module.
     *
     * @param strAccessCode The access code of user
     * @param authenticationService The AdminAuthentication
     * @return user The instance of an AdminDatabaseUser's object
     */
    public AdminDatabaseUser load( String strAccessCode, AdminAuthentication authenticationService )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_LOAD_USER );
        daoUtil.setString( 1, strAccessCode );
        daoUtil.executeQuery(  );

        if ( !daoUtil.next(  ) )
        {
            daoUtil.free(  );
            throw new AppException( "The line doesn't exist " );
        }

        String strUserName = daoUtil.getString( 1 );
        AdminDatabaseUser user = new AdminDatabaseUser( strUserName, authenticationService );
        user.setDateValidityPassword( daoUtil.getDate( 3 ) );
        user.setLastPassword( daoUtil.getString( 4 ) );

        daoUtil.free(  );

        return user;
    }

    /**
     * load the data of an user from the table provided by the database authentication module with criterias
     *
     * @param strLastName The last name of user
     * @param strFirstName The first name of user
     * @param strEmail The email of user
     * @param authenticationService The AdminAuthentication
     * @return user The instance of an AdminDatabaseUser's object
     */
    public Collection<AdminDatabaseUser> selectAllDatabaseUsers( String strLastName, String strFirstName,
        String strEmail, AdminAuthentication authenticationService )
    {
        Collection<AdminDatabaseUser> userList = new ArrayList<AdminDatabaseUser>(  );

        String strSql = SQL_QUERY_SELECT_ALL_DATABASE_USERS;
        int nCountCriterias = 0;

        // last name criteria
        if ( ( strLastName != null ) && ( !strLastName.equals( "" ) ) )
        {
            strSql += ( ( nCountCriterias > 0 ) ? "AND " : "WHERE " );
            strSql += "last_name LIKE ? ";
            nCountCriterias++;
        }

        // first name criteria
        if ( ( strFirstName != null ) && ( !strFirstName.equals( "" ) ) )
        {
            strSql += ( ( nCountCriterias > 0 ) ? "AND " : "WHERE " );
            strSql += "first_name LIKE ? ";
            nCountCriterias++;
        }

        // email criteria
        if ( ( strEmail != null ) && ( !strEmail.equals( "" ) ) )
        {
            strSql += ( ( nCountCriterias > 0 ) ? "AND " : "WHERE " );
            strSql += "email LIKE ? ";
            nCountCriterias++;
        }

        DAOUtil daoUtil = new DAOUtil( strSql );

        if ( ( strEmail != null ) && ( !strEmail.equals( "" ) ) )
        {
            daoUtil.setString( nCountCriterias, strEmail + '%' );
            nCountCriterias--;
        }

        if ( ( strFirstName != null ) && ( !strFirstName.equals( "" ) ) )
        {
            daoUtil.setString( nCountCriterias, strFirstName + '%' );
            nCountCriterias--;
        }

        if ( ( strLastName != null ) && ( !strLastName.equals( "" ) ) )
        {
            daoUtil.setString( nCountCriterias, strLastName + '%' );
            nCountCriterias--;
        }

        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            String strLogin = daoUtil.getString( 1 );
            AdminDatabaseUser user = new AdminDatabaseUser( strLogin, authenticationService );
            user.setLastName( daoUtil.getString( 2 ) );
            user.setFirstName( daoUtil.getString( 3 ) );
            user.setEmail( daoUtil.getString( 4 ) );
            userList.add( user );
        }

        daoUtil.free(  );

        return userList;
    }

    /**
     * load the data of an user from the table provided by the database authentication module
     * This provides public data specific to the database authentication module.
     *
     * @param strAccessCode The access code of user
     * @param authenticationService The AdminAuthentication
     * @return user The instance of an AdminDatabaseUser's object
     */
    public AdminUser selectUserPublicData( String strAccessCode, AdminAuthentication authenticationService )
    {
        AdminUser user = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_LOAD_USER );
        daoUtil.setString( 1, strAccessCode );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            user = new AdminUser( strAccessCode, authenticationService );
            user.setLastName( daoUtil.getString( 1 ) );
            user.setFirstName( daoUtil.getString( 2 ) );
            user.setEmail( daoUtil.getString( 3 ) );
        }

        daoUtil.free(  );

        return user;
    }
}
