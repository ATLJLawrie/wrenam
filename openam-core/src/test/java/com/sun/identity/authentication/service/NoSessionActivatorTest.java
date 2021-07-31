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
 * Copyright 2015-2016 ForgeRock AS.
 * Portions Copyright 2021 Wren Security.
 */

package com.sun.identity.authentication.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.testng.annotations.Test;
import org.wrensecurity.wrenam.test.AbstractMockBasedTest;

import com.iplanet.dpro.session.SessionID;
import com.iplanet.dpro.session.service.InternalSession;
import com.iplanet.dpro.session.service.SessionService;

public class NoSessionActivatorTest extends AbstractMockBasedTest {

    @Mock
    private SessionService mockSessionService;

    @Mock
    private InternalSession mockSession;

    @Test
    public void shouldDestroyAuthSession() throws AuthException {
        // Given
        final SessionID sid = new SessionID();
        given(mockSession.getID()).willReturn(sid);

        // When
        NoSessionActivator.INSTANCE.activateSession(null, mockSessionService, mockSession, null);

        // Then
        verify(mockSessionService).destroyAuthenticationSession(sid);
    }

    @Test
    public void shouldAlwaysReturnTrue() throws Exception {
        assertTrue(NoSessionActivator.INSTANCE.activateSession(null, mockSessionService, mockSession, null));
    }

}