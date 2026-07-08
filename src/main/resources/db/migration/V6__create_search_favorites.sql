CREATE TABLE IF NOT EXISTS search_favorites (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(120) NOT NULL,
  criteria_json TEXT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_search_favorites_created_at (created_at)
);
