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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.forgerock.openam.setup.EmbeddedOpenDJManager.State.CONFIGURED;
import static org.forgerock.openam.setup.EmbeddedOpenDJManager.State.NO_EMBEDDED_INSTANCE;
import static org.forgerock.openam.setup.EmbeddedOpenDJManager.State.UPGRADE_REQUIRED;
import static org.forgerock.openam.setup.TestSetupHelper.deleteDirectory;
import static org.forgerock.openam.setup.TestSetupHelper.extractZip;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Random;

import org.assertj.core.api.ThrowableAssert;
import org.forgerock.openam.upgrade.OpenDJUpgrader;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wrensecurity.wrenam.test.AbstractMockBasedTest;

import com.sun.identity.shared.debug.Debug;

public class EmbeddedOpenDJManagerTest extends AbstractMockBasedTest {

    private final Random random = new Random();

    @Mock
    private Debug logger;
    @Mock
    private OpenDJUpgrader upgrader;

    private File baseDirectoryZipExtractPath;
    private String baseDirectory;

    @BeforeMethod
    public void setup() {
        baseDirectoryZipExtractPath = new File("/tmp/base-directory-" + random.nextInt());
        baseDirectory = baseDirectoryZipExtractPath.getAbsolutePath() + File.separator + "base-directory";
    }

    @AfterMethod
    public void tearDown() throws Exception {
        deleteDirectory(baseDirectoryZipExtractPath);
    }

    @Test
    public void whenBaseDirectoryDoesNotExistItIsInNoEmbeddedInstanceState() {
        String baseDirectory = "/tmp/non-existent-base-directory";
        EmbeddedOpenDJManager embeddedOpenDJManager = new EmbeddedOpenDJManager(logger, baseDirectory, upgrader);
        assertThat(embeddedOpenDJManager.getState()).isEqualTo(NO_EMBEDDED_INSTANCE);
    }

    @Test
    public void whenOpenDJDirectoryDoesNotExistItIsInNoEmbeddedInstanceState() throws Exception {
        createBaseDirectory("empty-base-directory");
        EmbeddedOpenDJManager embeddedOpenDJManager = new EmbeddedOpenDJManager(logger, baseDirectory, upgrader);
        assertThat(embeddedOpenDJManager.getState()).isEqualTo(NO_EMBEDDED_INSTANCE);
    }

    @Test
    public void whenOpenDJDirectoryExistsAndDoesNotRequireUpgradeItIsInConfiguredState() throws Exception {
        EmbeddedOpenDJManager embeddedOpenDJManager = setupInstallOpenDJNotRequiringUpgrade();
        assertThat(embeddedOpenDJManager.getState()).isEqualTo(CONFIGURED);
    }

    @Test
    public void whenOpenDJDirectoryExistsAndRequireUpgradeItIsInUpgradeRequiredState() throws Exception {
        EmbeddedOpenDJManager embeddedOpenDJManager = setupInstallOpenDJRequiringUpgrade();
        assertThat(embeddedOpenDJManager.getState()).isEqualTo(UPGRADE_REQUIRED);
    }

    @Test
    public void whenInNoEmbeddedInstanceStateItShouldNotPerformUpgrade() throws Exception {
        createBaseDirectory("empty-base-directory");
        final EmbeddedOpenDJManager embeddedOpenDJManager = new EmbeddedOpenDJManager(logger, baseDirectory, upgrader);
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                embeddedOpenDJManager.upgrade();
            }
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot upgrade embedded instance as no embedded instance has been configured");
    }

    @Test
    public void whenInConfiguredStateItShouldNotPerformUpgrade() throws Exception {
        final EmbeddedOpenDJManager embeddedOpenDJManager = setupInstallOpenDJNotRequiringUpgrade();
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                embeddedOpenDJManager.upgrade();
            }
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Embedded instance does not require upgrading");
    }

    @Test
    public void whenInUpgradeRequiredStateItShouldPerformUpgrade() throws Exception {
        EmbeddedOpenDJManager embeddedOpenDJManager = setupInstallOpenDJRequiringUpgrade();
        embeddedOpenDJManager.upgrade();
        verify(upgrader).upgrade();
    }

    private EmbeddedOpenDJManager setupInstallOpenDJNotRequiringUpgrade() throws Exception {
        return setupInstallOpenDJRequiringUpgrade(false);
    }

    private EmbeddedOpenDJManager setupInstallOpenDJRequiringUpgrade() throws Exception {
        return setupInstallOpenDJRequiringUpgrade(true);
    }

    private EmbeddedOpenDJManager setupInstallOpenDJRequiringUpgrade(boolean isUpgradeRequired) throws Exception {
        createBaseDirectory("installed-opendj-base-directory");
        given(upgrader.isUpgradeRequired()).willReturn(isUpgradeRequired);
        return new EmbeddedOpenDJManager(logger, baseDirectory, upgrader);
    }

    private void createBaseDirectory(String baseDirectoryTemplateName) throws IOException, URISyntaxException {
        String zipFile = "setup" + File.separator + baseDirectoryTemplateName + ".zip";
        File baseDirectoryZipFile = Paths.get(getClass().getClassLoader().getResource(zipFile).toURI()).toFile();
        extractZip(baseDirectoryZipFile, baseDirectoryZipExtractPath.toPath());
    }
}
