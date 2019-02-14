package org.dexon.dekusan.sdk;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import dekusan.Response;
import dekusan.SignTransactionRequest;
import dekusan.DekuSan;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ResponseTest {
    @Test
    public void testIsAvailable() {
        Response response = new Response(null, null, DekuSan.ErrorCode.NONE);
        assertFalse(response.isAvailable());
        response = new Response(null, null, DekuSan.ErrorCode.CANCELED);
        assertFalse(response.isAvailable());
        response = new Response(null, "0x", DekuSan.ErrorCode.NONE);
        assertFalse(response.isAvailable());
        response = new Response(SignTransactionRequest.builder().get(), "0x", DekuSan.ErrorCode.NONE);
        assertTrue(response.isAvailable());
        response = new Response(SignTransactionRequest.builder().get(), null, DekuSan.ErrorCode.NONE);
        assertFalse(response.isAvailable());
        response = new Response(SignTransactionRequest.builder().get(), "0x", DekuSan.ErrorCode.CANCELED);
        assertTrue(response.isAvailable());
    }
}