-- V3__features.sql — reviews, payments, images, complaints

-- User reviews of PGs (separate from Google reviews)
CREATE TABLE reviews (
    id VARCHAR(255) PRIMARY KEY,
    pg_id VARCHAR(255) NOT NULL REFERENCES pgs(id),
    user_id VARCHAR(255) NOT NULL REFERENCES users(id),
    overall_rating INTEGER NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    food_rating INTEGER CHECK (food_rating BETWEEN 1 AND 5),
    safety_rating INTEGER CHECK (safety_rating BETWEEN 1 AND 5),
    cleanliness_rating INTEGER CHECK (cleanliness_rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (pg_id, user_id)
);

-- Razorpay payment records
CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL REFERENCES bookings(id),
    user_id VARCHAR(255) NOT NULL REFERENCES users(id),
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    amount INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- PG listing images (local file storage)
CREATE TABLE pg_images (
    id VARCHAR(255) PRIMARY KEY,
    pg_id VARCHAR(255) NOT NULL REFERENCES pgs(id),
    filename VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

-- Maintenance/support complaints from tenants
CREATE TABLE complaints (
    id VARCHAR(255) PRIMARY KEY,
    pg_id VARCHAR(255) REFERENCES pgs(id),
    user_id VARCHAR(255) NOT NULL REFERENCES users(id),
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Add owner_phone to pgs if not already present
ALTER TABLE pgs ADD COLUMN IF NOT EXISTS owner_phone VARCHAR(20);

-- Indexes
CREATE INDEX idx_reviews_pg ON reviews(pg_id);
CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_pg_images_pg ON pg_images(pg_id);
CREATE INDEX idx_complaints_user ON complaints(user_id);
