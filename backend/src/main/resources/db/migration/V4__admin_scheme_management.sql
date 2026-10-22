-- V4__admin_scheme_management.sql
-- Administrator Scheme Management, Source Verification, and Publication Controls

-- 1. Add publication status, verification status, source URL, and verification notes to schemes
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS publication_status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED';
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS verification_status VARCHAR(30) NOT NULL DEFAULT 'VERIFIED';
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS source_url VARCHAR(500);
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS verification_notes TEXT;

-- 2. Populate source_url from official_url where source_url is null
UPDATE schemes
SET source_url = official_url
WHERE source_url IS NULL AND official_url IS NOT NULL;

-- 3. Create indexes for fast filtering by publication and verification status
CREATE INDEX IF NOT EXISTS idx_schemes_publication_status ON schemes(publication_status);
CREATE INDEX IF NOT EXISTS idx_schemes_verification_status ON schemes(verification_status);
