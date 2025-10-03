# User Story Template

**Title:**
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**
1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**
- [Additional information or edge cases]

**Title:** Admin Secure Login
_As an **Admin**, I want to **log into the portal with my username and password** so that **I can manage the platform securely.**_

**Acceptance Criteria:**
1. The system must validate the provided username and password against the secure user database.
2. The user must be redirected to the main Admin dashboard upon successful login.
3. The system must display a clear error message for incorrect credentials.

**Priority:** High
**Story Points:** 3
**Notes:**
- Must use encrypted communication (e.g., HTTPS).

**Title:** Admin Portal Logout
_As an **Admin**, I want to **log out of the portal** so that **I can protect system access from unauthorized users.**_

**Acceptance Criteria:**
1. Clicking the 'Logout' button immediately terminates the active session.
2. The user is redirected to the main login page.
3. Attempting to navigate back to an Admin page after logout prompts a login request.

**Priority:** High
**Story Points:** 1
**Notes:**
- All session cookies or tokens must be invalidated upon logout.

**Title:** Add Doctor Profile
_As an **Admin**, I want to **add doctors to the portal** so that **patients can schedule appointments with the new medical staff.**_

**Acceptance Criteria:**
1. A form must be available to enter the doctor's name, specialty, contact, and initial login credentials.
2. The system must confirm successful creation and notify the Doctor with their temporary login details.
3. The system must prevent the creation of a profile with a duplicate username or email.

**Priority:** High
**Story Points:** 5
**Notes:**
- Doctor profiles must default to an 'Inactive' status until confirmed by the Admin.

**Title:** Remove Doctor Profile
_As an **Admin**, I want to **delete a doctor's profile from the portal** so that **their information and scheduling options are permanently removed from the system.**_

**Acceptance Criteria:**
1. The Admin must be prompted with a confirmation dialogue before deletion is finalized.
2. All future appointments associated with the deleted doctor must be marked as 'Cancelled' and affected patients must be notified.
3. The system must maintain an audit log of the deletion.

**Priority:** Medium
**Story Points:** 5
**Notes:**
- A soft-delete (archiving) option should be considered before a permanent hard-delete.

**Title:** Monthly Appointment Reporting
_As an **Admin**, I want to **view a report on the number of appointments per month** so that **I can track usage statistics and identify peak demand periods.**_

**Acceptance Criteria:**
1. The report must display a monthly count of 'Completed' and 'Cancelled' appointments over the last 12 months.
2. The report data must be filterable by Doctor specialty.
3. The report must be exportable as a CSV file.

**Priority:** Medium
**Story Points:** 8
**Notes:**
- This report will be powered by data extracted via the MySQL stored procedure.

**Title:** View Doctor Directory
_As a **Patient**, I want to **view a list of doctors without logging in** so that **I can explore options and specialties before registering.**_

**Acceptance Criteria:**
1. The public homepage displays a list of doctors, their specialties, and qualifications.
2. The list is filterable by specialty (e.g., Cardiology, Dermatology).
3. The system ensures no personal patient data is visible on this public page.

**Priority:** High
**Story Points:** 3
**Notes:**
- Must clearly indicate that booking requires login/registration.

**Title:** Patient Self-Registration
_As a **Patient**, I want to **sign up using my email and a password** so that **I can gain access to the booking and management features.**_

**Acceptance Criteria:**
1. The sign-up form validates the email address and requires a secure password.
2. The system confirms successful registration via email.
3. The user is prompted to complete any required initial profile information (e.g., name, DOB).

**Priority:** High
**Story Points:** 5
**Notes:**
- Password requirements must be clearly communicated.

**Title:** Patient Portal Login
_As a **Patient**, I want to **log into the portal** so that **I can securely manage my bookings and personal health profile.**_

**Acceptance Criteria:**
1. The system authenticates the user's email and password.
2. Successful login directs the patient to their appointment dashboard.
3. The system must display a helpful error message for failed login attempts.

**Priority:** High
**Story Points:** 3
**Notes:**
- Login attempts should be rate-limited to prevent brute-force attacks.

**Title:** Book Extended Consultation
_As a **Patient**, I want to **log in and book an hour-long appointment** so that **I can consult with a doctor for a complex or comprehensive issue.**_

**Acceptance Criteria:**
1. The user must be able to select a 60-minute duration during the booking process.
2. Only time slots available for 60-minute bookings are displayed.
3. The booking process reserves the full hour and sends a confirmation notification.

**Priority:** High
**Story Points:** 8
**Notes:**
- This feature is prioritized for new patient evaluations or complex cases.

**Title:** Review Upcoming Schedule
_As a **Patient**, I want to **view a list of my upcoming appointments** so that **I can prepare accordingly (e.g., plan travel, gather medical history).**_

**Acceptance Criteria:**
1. The list clearly shows the date, time, doctor's name, and specialty.
2. The list is easily accessible from the main dashboard after login.
3. Appointments are sorted chronologically, with the soonest one first.

**Priority:** High
**Story Points:** 3
**Notes:**
- Should include an option to add the appointment to a personal calendar (iCal/Google).

**Title:** Account Logout
_As a **Patient**, I want to **log out of the portal** so that **I can secure my account and protect my private medical data.**_

**Acceptance Criteria:**
1. Clicking the 'Logout' button immediately terminates the active session.
2. The user is redirected to the login screen or public homepage.
3. All sensitive data cached on the client side (if any) is cleared upon logout.

**Priority:** Medium
**Story Points:** 1
**Notes:**
- Session expiration should be set to automatically log users out after a period of inactivity.

**Title:** Doctor Portal Login
_As a **Doctor**, I want to **log into the portal** so that **I can manage my schedule and appointments securely.**_

**Acceptance Criteria:**
1. The system authenticates the user's credentials against the registered doctor profile.
2. Successful login directs the Doctor to their primary scheduling dashboard.
3. The system enforces two-factor authentication (2FA) for added security.

**Priority:** High
**Story Points:** 3
**Notes:**
- Must adhere to clinic security policies regarding session management.

**Title:** Secure Account Logout
_As a **Doctor**, I want to **log out of the portal** so that **I can protect my patient data and secure my account.**_

**Acceptance Criteria:**
1. Clicking the 'Logout' button immediately and permanently terminates the active session.
2. The user is redirected to the login screen.
3. Attempting to access sensitive pages after logout requires re-authentication.

**Priority:** High
**Story Points:** 1
**Notes:**
- Session data must be cleared from the local environment upon logout.

**Title:** Daily/Weekly Appointment Calendar
_As a **Doctor**, I want to **view my appointment calendar** so that **I can stay organized and prepare for my upcoming consultations.**_

**Acceptance Criteria:**
1. The calendar must show a day view, week view, and month view.
2. Each appointment block clearly displays the patient's name and the scheduled duration.
3. The calendar is easily accessible from the main dashboard.

**Priority:** High
**Story Points:** 5
**Notes:**
- Should allow printing the daily schedule.

**Title:** Block Time Slots
_As a **Doctor**, I want to **mark specific times as unavailable** so that **patients are only shown accurate, available slots for booking.**_

**Acceptance Criteria:**
1. I can easily select and block off one-time or recurring periods (e.g., lunch breaks, vacation).
2. The system confirms the block-out time and immediately removes those slots from patient view.
3. The system prevents marking time as unavailable if an appointment is already booked in that slot.

**Priority:** High
**Story Points:** 8
**Notes:**
- A notification should be sent to the Admin for significant blocks of time off.

**Title:** Profile Information Update
_As a **Doctor**, I want to **update my profile with specialization and contact information** so that **patients have up-to-date information before they book an appointment.**_

**Acceptance Criteria:**
1. An edit form allows modifying professional details (specialty, clinic phone, bio).
2. The system validates mandatory fields before saving the update.
3. Updates are immediately reflected in the public-facing Doctor directory.

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Major changes (e.g., primary specialty) may require Admin approval.

**Title:** View Patient Appointment Details
_As a **Doctor**, I want to **view the patient details and reason for visit for upcoming appointments** so that **I can be prepared and provide appropriate care.**_

**Acceptance Criteria:**
1. Clicking on an appointment in the calendar reveals a secure summary panel.
2. The panel displays the patient's name, age, contact info, and stated reason for the visit.
3. Access to these details is strictly limited to the Doctor assigned to the appointment.

**Priority:** High
**Story Points:** 5
**Notes:**
- The system must comply with all privacy regulations (e.g., HIPAA) for viewing patient data.
