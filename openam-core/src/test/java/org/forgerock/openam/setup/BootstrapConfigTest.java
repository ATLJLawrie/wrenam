/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2021 Wren Security.
 */
package org.forgerock.openam.setup;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test for bootstrap config
 */
public class BootstrapConfigTest {

    private static final String TEST_ENVIRONMENT_NAME = BootstrapConfigTest.class.getName();

    private static final String TEST_ENVIRONMENT_VALUE = "TEST ENVIRONMENT";

    private static final String TEST_PROPERTY_NAME = BootstrapConfigTest.class.getName();

    private static final String TEST_PROPERTY_VALUE = "TEST PROPERTY";

    @BeforeTest
    public void setup() {
        BootstrapConfig.ENVIRONMENT_OVERRIDE = Collections
                .singletonMap(TEST_ENVIRONMENT_NAME, TEST_ENVIRONMENT_VALUE);
        System.setProperty(TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);
    }

    @AfterTest
    public void teardown() {
        BootstrapConfig.ENVIRONMENT_OVERRIDE = null;
        System.clearProperty(TEST_PROPERTY_NAME);
    }

    @Test
    public void testEnvVarExpansion() {
        String instance = "HELLO ${env." + TEST_ENVIRONMENT_NAME + "} WORLD ${" + TEST_PROPERTY_NAME + "}";
        String expected = "HELLO " + TEST_ENVIRONMENT_VALUE + " WORLD " + TEST_ENVIRONMENT_VALUE;

        assertThat(BootstrapConfig.expandEnvironmentVariables(instance).equals(expected));

        // test variable not found
        String s = "This is ${baz.property} is ${env.NOTFOUND}";
        assertThat(BootstrapConfig.expandEnvironmentVariables(s).equals(s));
    }

    @Test
    public void testBasicBootConfig() throws IOException {
        String instance = "HELLO ${" + TEST_PROPERTY_NAME + "} WORLD";
        String expected = "HELLO " + TEST_PROPERTY_VALUE + " WORLD";

        BootstrapConfig bs = new BootstrapConfig();
        bs.setInstance(instance);

        String s = bs.toJson();
        String env = BootstrapConfig.expandEnvironmentVariables(s);

        // test marshall back in
        BootstrapConfig bs2 = BootstrapConfig.fromJson(env);

        assertThat(bs2.getInstance().equals(expected));
    }

    @Test
    public void testJsonInit() throws IOException {
        String json = "{\n" +
                "  \"instance\" : \"${env." + TEST_ENVIRONMENT_NAME + "}\",\n" +
                "  \"dsameUser\" : \"cn=dsameuser,ou=DSAME Users,dc=openam,dc=forgerock,dc=org\",\n" +
                "  \"keystores\" : {\n" +
                "    \"default\" : {\n" +
                "      \"keyStorePasswordFile\" : \"${env.OPENAM_SECRETS}/.storepass\",\n" +
                "      \"keyPasswordFile\" : \"${env.OPENAM_SECRETS}/.keypass\",\n" +
                "      \"keyStoreType\" : \"JCEKS\",\n" +
                "      \"keyStoreFile\" : \"${env.OPENAM_SECRETS}/keystore.jceks\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"configStoreList\" : [ {\n" +
                "    \"baseDN\" : \"dc=openam,dc=forgerock,dc=org\",\n" +
                "    \"dirManagerDN\" : \"cn=Directory Manager\",\n" +
                "    \"ldapHost\" : \"${env.OPENAM_CONFIG_STORE_LDAP_HOST}\",\n" +
                "    \"ldapPort\" : 389,\n" +
                "    \"ldapProtocol\" : \"ldap\"\n" +
                "  } ]\n" +
                "}";

        BootstrapConfig config = BootstrapConfig.fromJson(json);
        assertThat(config.getInstance().equals(TEST_ENVIRONMENT_VALUE));

        ConfigStoreProperties p = config.getConfigStoreList().get(0);
        assertThat(p.getLdapPort() == 389);
    }

}
