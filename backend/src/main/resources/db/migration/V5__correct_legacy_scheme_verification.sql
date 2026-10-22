-- V5__correct_legacy_scheme_verification.sql
-- Correct legacy scheme verification status:
-- Schemes without recorded affirmative administrative review notes are updated from 'VERIFIED' default to 'NEEDS_REVIEW'.
-- Alter column default for verification_status to 'UNVERIFIED'.

-- 1. Flag schemes lacking affirmative review notes as NEEDS_REVIEW
UPDATE schemes
SET verification_status = 'NEEDS_REVIEW'
WHERE verification_notes IS NULL OR TRIM(verification_notes) = '';

-- 2. Alter column default for verification_status to 'UNVERIFIED'
ALTER TABLE schemes ALTER COLUMN verification_status SET DEFAULT 'UNVERIFIED';
