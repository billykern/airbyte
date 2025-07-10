/*
 * Copyright (c) 2024 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.s3_v2

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class S3V2ConfigurationTest {

    @Test
    fun testChecksumEnabledMapsToS3ClientConfiguration() {
        val configJson = """
        {
            "s3_bucket_name": "test-bucket",
            "s3_bucket_path": "test-path",
            "s3_bucket_region": "us-east-1",
            "access_key_id": "test-key",
            "secret_access_key": "test-secret",
            "checksumEnabled": true
        }
        """.trimIndent()

        val spec = ObjectMapper().readValue(configJson, S3V2Specification::class.java)
        assertTrue(spec.checksumEnabled, "checksumEnabled should be true when set in JSON")

        // Test configuration mapping
        val config = S3V2Configuration(
            awsAccessKeyConfiguration = spec.toAWSAccessKeyConfiguration(),
            awsArnRoleConfiguration = spec.toAWSArnRoleConfiguration(),
            s3BucketConfiguration = spec.toS3BucketConfiguration(),
            objectStoragePathConfiguration = spec.toObjectStoragePathConfiguration(),
            objectStorageFormatConfiguration = spec.toObjectStorageFormatConfiguration(),
            objectStorageCompressionConfiguration = spec.toCompressionConfiguration(),
            checksumEnabled = spec.checksumEnabled
        )

        assertTrue(config.s3ClientConfiguration.objectLockChecksumEnabled,
            "objectLockChecksumEnabled should be true when checksumEnabled=true")
    }

    @Test
    fun testChecksumDisabledByDefault() {
        val configJson = """
        {
            "s3_bucket_name": "test-bucket",
            "s3_bucket_path": "test-path",
            "s3_bucket_region": "us-east-1",
            "access_key_id": "test-key",
            "secret_access_key": "test-secret"
        }
        """.trimIndent()

        val spec = ObjectMapper().readValue(configJson, S3V2Specification::class.java)
        assertFalse(spec.checksumEnabled, "checksumEnabled should default to false")

        // Test configuration mapping
        val config = S3V2Configuration(
            awsAccessKeyConfiguration = spec.toAWSAccessKeyConfiguration(),
            awsArnRoleConfiguration = spec.toAWSArnRoleConfiguration(),
            s3BucketConfiguration = spec.toS3BucketConfiguration(),
            objectStoragePathConfiguration = spec.toObjectStoragePathConfiguration(),
            objectStorageFormatConfiguration = spec.toObjectStorageFormatConfiguration(),
            objectStorageCompressionConfiguration = spec.toCompressionConfiguration(),
            checksumEnabled = spec.checksumEnabled
        )

        assertFalse(config.s3ClientConfiguration.objectLockChecksumEnabled,
            "objectLockChecksumEnabled should be false when checksumEnabled=false")
    }

    @Test
    fun testChecksumEnabledExplicitlyDisabled() {
        val configJson = """
        {
            "s3_bucket_name": "test-bucket",
            "s3_bucket_path": "test-path", 
            "s3_bucket_region": "us-east-1",
            "access_key_id": "test-key",
            "secret_access_key": "test-secret",
            "checksumEnabled": false
        }
        """.trimIndent()

        val spec = ObjectMapper().readValue(configJson, S3V2Specification::class.java)
        assertFalse(spec.checksumEnabled, "checksumEnabled should be false when explicitly set")

        // Test configuration mapping
        val config = S3V2Configuration(
            awsAccessKeyConfiguration = spec.toAWSAccessKeyConfiguration(),
            awsArnRoleConfiguration = spec.toAWSArnRoleConfiguration(),
            s3BucketConfiguration = spec.toS3BucketConfiguration(),
            objectStoragePathConfiguration = spec.toObjectStoragePathConfiguration(),
            objectStorageFormatConfiguration = spec.toObjectStorageFormatConfiguration(),
            objectStorageCompressionConfiguration = spec.toCompressionConfiguration(),
            checksumEnabled = spec.checksumEnabled
        )

        assertFalse(config.s3ClientConfiguration.objectLockChecksumEnabled,
            "objectLockChecksumEnabled should be false when checksumEnabled=false")
    }
}
