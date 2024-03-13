package com.uber.sdk2.auth.api.request

import android.content.Context
import android.content.res.Resources
import com.uber.sdk2.auth.RobolectricTestBase
import com.uber.sdk2.auth.api.request.SsoConfigProvider.getSsoConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream

class SsoConfigTest: RobolectricTestBase() {

    @Test
    fun `getSsoConfig returns valid config`() {
        val context = mock<Context>()
        val resources = mock<Resources>()
        val resourceId = 1
        val configJsonString = """{"client_id":"testClientId","redirect_uri":"testRedirectUri","scope":"testScope"}"""

        whenever(context.resources).thenReturn(resources)
        whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
        whenever(resources.getIdentifier(any(), any(), any())).thenReturn(resourceId)
        whenever(resources.openRawResource(anyInt())).thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

        val result = getSsoConfig(context)

        assertEquals("testClientId", result.clientId)
        assertEquals("testRedirectUri", result.redirectUri)
        assertEquals("testScope", result.scope)
    }

    @Test
    fun `getSsoConfig returns valid config with no scope`() {
        val context = mock<Context>()
        val resources = mock<Resources>()
        val resourceId = 1
        val configJsonString = """{"client_id":"testClientId","redirect_uri":"testRedirectUri"}"""

        whenever(context.resources).thenReturn(resources)
        whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
        whenever(resources.getIdentifier(any(), any(), any())).thenReturn(resourceId)
        whenever(resources.openRawResource(anyInt())).thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

        val result = getSsoConfig(context)

        assertEquals("testClientId", result.clientId)
        assertEquals("testRedirectUri", result.redirectUri)
        assertEquals(null, result.scope)
    }

    @Test(expected = Exception::class)
    fun `getSsoConfig throws exception when config file is not found`() {
        val context = mock<Context>()
        val resources = mock<Resources>()
        val resourceId = 0

        whenever(context.resources).thenReturn(resources)
        whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
        whenever(resources.getIdentifier(any(), any(), any())).thenReturn(resourceId)

        getSsoConfig(context)
    }

    @Test(expected = Exception::class)
    fun `getSsoConfig throws exception when config file is empty`() {
        val context = mock<Context>()
        val resources = mock<Resources>()
        val resourceId = 1
        val configJsonString = ""

        whenever(context.resources).thenReturn(resources)
        whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
        whenever(resources.getIdentifier(any(), any(), any())).thenReturn(resourceId)
        whenever(resources.openRawResource(anyInt())).thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

        getSsoConfig(context)
    }

    @Test(expected = Exception::class)
    fun `getSsoConfig throws exception when config file is invalid`() {
        val context = mock<Context>()
        val resources = mock<Resources>()
        val resourceId = 1
        val configJsonString = """{"client_id":"testClientId"}"""

        whenever(context.resources).thenReturn(resources)
        whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
        whenever(resources.getIdentifier(any(), any(), any())).thenReturn(resourceId)
        whenever(resources.openRawResource(anyInt())).thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

        getSsoConfig(context)
    }

    @Test(expected = Exception::class)
    fun `getSsoConfig throws exception when config file is missing required fields`() {
        val context = mock<Context>()
        val resources = mock<Resources>()
        val resourceId = 1
        val configJsonString = """{}"""

        whenever(context.resources).thenReturn(resources)
        whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
        whenever(resources.getIdentifier(any(), any(), any())).thenReturn(resourceId)
        whenever(resources.openRawResource(anyInt())).thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

        getSsoConfig(context)
    }
}