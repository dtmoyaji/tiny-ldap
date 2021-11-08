/*
 * Copyright 2021 bythe.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tiny.core.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Ldap Client
 *
 * @author daianjimax
 */
public class LdapClient {

    private HashMap env;
    private DirContext context;

    private String partition;
    
    public LdapClient(){
        this.env = new HashMap();
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getPartition() {
        return this.partition;
    }

    public static void main(String[] args) {

        LdapClient client = new LdapClient();
        client.init();
        NamingEnumeration results = client.get();
        client.close();

        try {
            while (results != null && results.hasMore()) {
                SearchResult si = (SearchResult) results.next();

                /* エントリ名の出力 */
                System.out.println("name: " + si.getName());

                Attributes attrs = si.getAttributes();
                if (attrs == null) {
                    System.out.println("No attributes");
                } else {
                    /* 属性の出力 */
                    for (NamingEnumeration ae = attrs.getAll();
                            ae.hasMoreElements();) {
                        Attribute attr = (Attribute) ae.next();
                        String attrId = attr.getID();

                        /* 属性値の出力 */
                        for (Enumeration vals = attr.getAll();
                                vals.hasMoreElements();
                                System.out.println(attrId + ": " + vals.nextElement()));
                    }
                }
                System.out.println();
            }
        } catch (Exception ex) {
            Logger.getLogger(LdapClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(
                client.auth("",
                        "")
        );
        
        client.close();
    }

    public DirContext init() {
        this.context = null;
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "LDAPSVR"); //LDAPサーバ
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "LDAP_NODE"); //ID, 組織
        env.put(Context.SECURITY_CREDENTIALS, "PWD"); //パスワード
        try {
            this.context = new InitialDirContext(new Hashtable(env));
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
        return this.context;
    }

    public NamingEnumeration get() {
        NamingEnumeration results = null;
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            results = this.context.search("", "", constraints);
        } catch (NamingException ex) {
            Logger.getLogger(LdapClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    public boolean auth(String uid, String password) {
        boolean res = false;

        //LDAP接続情報
        env.put(Context.SECURITY_PRINCIPAL, uid); //ID, 組織
        env.put(Context.SECURITY_CREDENTIALS, password); //パスワード

        try {
            this.context = new InitialDirContext(new Hashtable(env));
            this.context.close();
            System.out.println("auth ok");
            res = true;
        } catch (NamingException ex) {
            Logger.getLogger(LdapClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }

    public void close() {
        try {
            if (this.context != null) {
                this.context.close();
            }
        } catch (NamingException ex) {
            Logger.getLogger(LdapClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}