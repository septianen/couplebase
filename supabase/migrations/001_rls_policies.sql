-- Couplebase Row-Level Security (RLS) Policies
-- All data is scoped to couple_id so partners can only access their own data.
-- Requires: auth.uid() returns the logged-in user's ID
-- Requires: user_profile table links user ID to couple_id

-- Helper function: get the couple_id for the current user
CREATE OR REPLACE FUNCTION get_my_couple_id()
RETURNS TEXT AS $$
  SELECT couple_id FROM user_profile WHERE id = auth.uid()
$$ LANGUAGE sql SECURITY DEFINER STABLE;

-- ============================================================
-- user_profile
-- ============================================================
ALTER TABLE user_profile ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own profile"
  ON user_profile FOR SELECT
  USING (id = auth.uid());

CREATE POLICY "Users can read partner profile"
  ON user_profile FOR SELECT
  USING (couple_id = get_my_couple_id());

CREATE POLICY "Users can update own profile"
  ON user_profile FOR UPDATE
  USING (id = auth.uid());

CREATE POLICY "Users can insert own profile"
  ON user_profile FOR INSERT
  WITH CHECK (id = auth.uid());

-- ============================================================
-- couple
-- ============================================================
ALTER TABLE couple ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Couple members can read their couple"
  ON couple FOR SELECT
  USING (id = get_my_couple_id());

CREATE POLICY "Users can create a couple"
  ON couple FOR INSERT
  WITH CHECK (partner1_id = auth.uid() OR partner2_id = auth.uid());

CREATE POLICY "Couple members can update their couple"
  ON couple FOR UPDATE
  USING (id = get_my_couple_id());

-- ============================================================
-- Macro: couple-scoped table RLS
-- Applied to all tables that have a couple_id column
-- ============================================================

-- checklist_item
ALTER TABLE checklist_item ENABLE ROW LEVEL SECURITY;

CREATE POLICY "checklist_item_select" ON checklist_item FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "checklist_item_insert" ON checklist_item FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "checklist_item_update" ON checklist_item FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "checklist_item_delete" ON checklist_item FOR DELETE
  USING (couple_id = get_my_couple_id());

-- budget_category
ALTER TABLE budget_category ENABLE ROW LEVEL SECURITY;

CREATE POLICY "budget_category_select" ON budget_category FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "budget_category_insert" ON budget_category FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "budget_category_update" ON budget_category FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "budget_category_delete" ON budget_category FOR DELETE
  USING (couple_id = get_my_couple_id());

-- expense
ALTER TABLE expense ENABLE ROW LEVEL SECURITY;

CREATE POLICY "expense_select" ON expense FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "expense_insert" ON expense FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "expense_update" ON expense FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "expense_delete" ON expense FOR DELETE
  USING (couple_id = get_my_couple_id());

-- guest
ALTER TABLE guest ENABLE ROW LEVEL SECURITY;

CREATE POLICY "guest_select" ON guest FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "guest_insert" ON guest FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "guest_update" ON guest FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "guest_delete" ON guest FOR DELETE
  USING (couple_id = get_my_couple_id());

-- vendor
ALTER TABLE vendor ENABLE ROW LEVEL SECURITY;

CREATE POLICY "vendor_select" ON vendor FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "vendor_insert" ON vendor FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "vendor_update" ON vendor FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "vendor_delete" ON vendor FOR DELETE
  USING (couple_id = get_my_couple_id());

-- vendor_payment
ALTER TABLE vendor_payment ENABLE ROW LEVEL SECURITY;

CREATE POLICY "vendor_payment_select" ON vendor_payment FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "vendor_payment_insert" ON vendor_payment FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "vendor_payment_update" ON vendor_payment FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "vendor_payment_delete" ON vendor_payment FOR DELETE
  USING (couple_id = get_my_couple_id());

-- timeline_block
ALTER TABLE timeline_block ENABLE ROW LEVEL SECURITY;

CREATE POLICY "timeline_block_select" ON timeline_block FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "timeline_block_insert" ON timeline_block FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "timeline_block_update" ON timeline_block FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "timeline_block_delete" ON timeline_block FOR DELETE
  USING (couple_id = get_my_couple_id());

-- milestone
ALTER TABLE milestone ENABLE ROW LEVEL SECURITY;

CREATE POLICY "milestone_select" ON milestone FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "milestone_insert" ON milestone FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "milestone_update" ON milestone FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "milestone_delete" ON milestone FOR DELETE
  USING (couple_id = get_my_couple_id());

-- life_goal
ALTER TABLE life_goal ENABLE ROW LEVEL SECURITY;

CREATE POLICY "life_goal_select" ON life_goal FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "life_goal_insert" ON life_goal FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "life_goal_update" ON life_goal FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "life_goal_delete" ON life_goal FOR DELETE
  USING (couple_id = get_my_couple_id());

-- goal_milestone
ALTER TABLE goal_milestone ENABLE ROW LEVEL SECURITY;

CREATE POLICY "goal_milestone_select" ON goal_milestone FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "goal_milestone_insert" ON goal_milestone FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "goal_milestone_update" ON goal_milestone FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "goal_milestone_delete" ON goal_milestone FOR DELETE
  USING (couple_id = get_my_couple_id());

-- monthly_budget
ALTER TABLE monthly_budget ENABLE ROW LEVEL SECURITY;

CREATE POLICY "monthly_budget_select" ON monthly_budget FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "monthly_budget_insert" ON monthly_budget FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "monthly_budget_update" ON monthly_budget FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "monthly_budget_delete" ON monthly_budget FOR DELETE
  USING (couple_id = get_my_couple_id());

-- savings_goal
ALTER TABLE savings_goal ENABLE ROW LEVEL SECURITY;

CREATE POLICY "savings_goal_select" ON savings_goal FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "savings_goal_insert" ON savings_goal FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "savings_goal_update" ON savings_goal FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "savings_goal_delete" ON savings_goal FOR DELETE
  USING (couple_id = get_my_couple_id());

-- savings_contribution
ALTER TABLE savings_contribution ENABLE ROW LEVEL SECURITY;

CREATE POLICY "savings_contribution_select" ON savings_contribution FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "savings_contribution_insert" ON savings_contribution FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "savings_contribution_update" ON savings_contribution FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "savings_contribution_delete" ON savings_contribution FOR DELETE
  USING (couple_id = get_my_couple_id());

-- shared_note
ALTER TABLE shared_note ENABLE ROW LEVEL SECURITY;

CREATE POLICY "shared_note_select" ON shared_note FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "shared_note_insert" ON shared_note FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "shared_note_update" ON shared_note FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "shared_note_delete" ON shared_note FOR DELETE
  USING (couple_id = get_my_couple_id());

-- journal_entry
ALTER TABLE journal_entry ENABLE ROW LEVEL SECURITY;

CREATE POLICY "journal_entry_select" ON journal_entry FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "journal_entry_insert" ON journal_entry FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "journal_entry_update" ON journal_entry FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "journal_entry_delete" ON journal_entry FOR DELETE
  USING (couple_id = get_my_couple_id());

-- journal_photo
ALTER TABLE journal_photo ENABLE ROW LEVEL SECURITY;

CREATE POLICY "journal_photo_select" ON journal_photo FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "journal_photo_insert" ON journal_photo FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "journal_photo_update" ON journal_photo FOR UPDATE
  USING (couple_id = get_my_couple_id());
CREATE POLICY "journal_photo_delete" ON journal_photo FOR DELETE
  USING (couple_id = get_my_couple_id());

-- daily_checkin
ALTER TABLE daily_checkin ENABLE ROW LEVEL SECURITY;

CREATE POLICY "daily_checkin_select" ON daily_checkin FOR SELECT
  USING (couple_id = get_my_couple_id());
CREATE POLICY "daily_checkin_insert" ON daily_checkin FOR INSERT
  WITH CHECK (couple_id = get_my_couple_id());
CREATE POLICY "daily_checkin_update" ON daily_checkin FOR UPDATE
  USING (couple_id = get_my_couple_id());

-- sync_queue is local-only, no RLS needed
