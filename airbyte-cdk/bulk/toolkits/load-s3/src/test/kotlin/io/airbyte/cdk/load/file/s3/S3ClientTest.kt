/*
 * Copyright (c) 2024 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.cdk.load.file.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ChecksumAlgorithm
import aws.sdk.kotlin.services.s3.model.CreateMultipartUploadRequest
import aws.sdk.kotlin.services.s3.model.CreateMultipartUploadResponse
import io.airbyte.cdk.load.command.s3.S3BucketConfiguration
import io.airbyte.cdk.load.command.s3.S3ClientConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class S3ClientTest {

    private val bucketConfig = S3BucketConfiguration(
        s3BucketName = "test-bucket",
        s3BucketRegion = "us-east-1",
        s3Endpoint = null
    )

    @Test
    fun testChecksumEnabledSetsAlgorithmInMultipartUpload() = runBlocking {
        // Mock AWS SDK client
        val mockS3Client = mockk<S3Client>()
        val requestSlot = slot<CreateMultipartUploadRequest>()
        
        // Mock response
        val mockResponse = CreateMultipartUploadResponse {
            uploadId = "test-upload-id"
            key = "test-key"
        }
        
        coEvery { mockS3Client.createMultipartUpload(capture(requestSlot)) } returns mockResponse

        // Test with checksum enabled
        val clientConfig = S3ClientConfiguration(objectLockChecksumEnabled = true)
        val s3KotlinClient = S3KotlinClient(mockS3Client, bucketConfig, clientConfig)

        // Start streaming upload
        s3KotlinClient.startStreamingUpload("test-key", emptyMap())

        // Verify checksum algorithm was set
        coVerify { mockS3Client.createMultipartUpload(any()) }
        val capturedRequest = requestSlot.captured
        assertEquals(ChecksumAlgorithm.Sha256, capturedRequest.checksumAlgorithm,
            "ChecksumAlgorithm should be SHA256 when objectLockChecksumEnabled=true")
        assertEquals("test-bucket", capturedRequest.bucket)
        assertEquals("test-key", capturedRequest.key)
    }

    @Test
    fun testChecksumDisabledDoesNotSetAlgorithm() = runBlocking {
        // Mock AWS SDK client
        val mockS3Client = mockk<S3Client>()
        val requestSlot = slot<CreateMultipartUploadRequest>()
        
        // Mock response
        val mockResponse = CreateMultipartUploadResponse {
            uploadId = "test-upload-id"
            key = "test-key"
        }
        
        coEvery { mockS3Client.createMultipartUpload(capture(requestSlot)) } returns mockResponse

        // Test with checksum disabled (default)
        val clientConfig = S3ClientConfiguration(objectLockChecksumEnabled = false)
        val s3KotlinClient = S3KotlinClient(mockS3Client, bucketConfig, clientConfig)

        // Start streaming upload
        s3KotlinClient.startStreamingUpload("test-key", emptyMap())

        // Verify checksum algorithm was NOT set
        coVerify { mockS3Client.createMultipartUpload(any()) }
        val capturedRequest = requestSlot.captured
        assertNull(capturedRequest.checksumAlgorithm,
            "ChecksumAlgorithm should be null when objectLockChecksumEnabled=false")
        assertEquals("test-bucket", capturedRequest.bucket)
        assertEquals("test-key", capturedRequest.key)
    }

    @Test
    fun testDefaultClientConfigurationHasChecksumDisabled() = runBlocking {
        // Mock AWS SDK client
        val mockS3Client = mockk<S3Client>()
        val requestSlot = slot<CreateMultipartUploadRequest>()
        
        // Mock response
        val mockResponse = CreateMultipartUploadResponse {
            uploadId = "test-upload-id"
            key = "test-key"
        }
        
        coEvery { mockS3Client.createMultipartUpload(capture(requestSlot)) } returns mockResponse

        // Test with default client configuration
        val s3KotlinClient = S3KotlinClient(mockS3Client, bucketConfig)

        // Start streaming upload
        s3KotlinClient.startStreamingUpload("test-key", emptyMap())

        // Verify checksum algorithm was NOT set (default behavior)
        coVerify { mockS3Client.createMultipartUpload(any()) }
        val capturedRequest = requestSlot.captured
        assertNull(capturedRequest.checksumAlgorithm,
            "ChecksumAlgorithm should be null with default configuration")
    }
}
