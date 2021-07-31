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

package org.forgerock.openam.blacklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.forgerock.bloomfilter.BloomFilter;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wrensecurity.wrenam.test.AbstractMockBasedTest;

import com.iplanet.dpro.session.Session;

public class BloomFilterBlacklistTest extends AbstractMockBasedTest {

    private static final long PURGE_DELAY = 1000L;

    @Mock
    private Blacklist<Blacklistable> mockDelegate;

    @Mock
    private BloomFilter<BloomFilterBlacklist.BlacklistEntry> mockBloomFilter;

    @Mock
    private Session mockSession;

    private BloomFilterBlacklist<Blacklistable> testBlacklist;

    @BeforeMethod
    public void setup() {
        testBlacklist = new BloomFilterBlacklist<>(mockDelegate, PURGE_DELAY, mockBloomFilter);
    }

    @Test
    public void shouldSubscribeForUpdatesFromOtherServers() {
        verify(mockDelegate).subscribe(any(Blacklist.Listener.class));
    }

    @Test
    public void shouldAddNotifiedBlacklistedSessionsToTheBloomFilter() {
        // Given
        ArgumentCaptor<Blacklist.Listener> listenerArgumentCaptor
                = ArgumentCaptor.forClass(Blacklist.Listener.class);
        willDoNothing().given(mockDelegate).subscribe(listenerArgumentCaptor.capture());
        testBlacklist = new BloomFilterBlacklist<>(mockDelegate, PURGE_DELAY, mockBloomFilter);
        Blacklist.Listener listener = listenerArgumentCaptor.getValue();
        String id = "testSession";
        long expiryTime = 1234l;

        // When
        listener.onBlacklisted(id, expiryTime);

        // Then
        verify(mockBloomFilter).add(new BloomFilterBlacklist.BlacklistEntry(id, expiryTime));
    }

    @Test
    public void shouldDelegateBlacklistToDelegate() throws Exception {
        testBlacklist.blacklist(mockSession);
        verify(mockDelegate).blacklist(mockSession);
    }

    @Test
    public void shouldNotCheckDelegateIfSessionNotInBloomFilter() throws Exception {
        // Given
        String id = "testSession";
        long expiryTime = 1234l;
        given(mockSession.getStableStorageID()).willReturn(id);
        given(mockSession.getBlacklistExpiryTime()).willReturn(expiryTime);
        given(mockBloomFilter.mightContain(new BloomFilterBlacklist.BlacklistEntry(id, expiryTime)))
                .willReturn(false);

        // When
        boolean result = testBlacklist.isBlacklisted(mockSession);

        // Then
        assertThat(result).isFalse();
        verify(mockDelegate, never()).isBlacklisted(any(Session.class));
    }

    @Test
    public void shouldCheckDelegateIfSessionIsInBloomFilter() throws Exception {
        // Given
        String id = "testSession";
        long expiryTime = 1234L;
        given(mockSession.getStableStorageID()).willReturn(id);
        given(mockSession.getBlacklistExpiryTime()).willReturn(expiryTime);
        given(mockBloomFilter.mightContain(new BloomFilterBlacklist.BlacklistEntry(id, expiryTime + PURGE_DELAY)))
                .willReturn(true);
        given(mockDelegate.isBlacklisted(mockSession)).willReturn(true);

        // When
        boolean result = testBlacklist.isBlacklisted(mockSession);

        // Then
        assertThat(result).isTrue();
        verify(mockDelegate).isBlacklisted(mockSession);
    }

    @Test
    public void shouldDelegateSubscriptions() {
        // Given
        Blacklist.Listener listener = mock(Blacklist.Listener.class);
        // When
        testBlacklist.subscribe(listener);
        // Then
        mockDelegate.subscribe(listener);
    }
}