# Stage 63P-D Doctor/Assistant device checklist

- Controlled-pilot login offers Doctor and Assistant roles.
- Doctor creates a one-time Assistant invitation for the owned clinic.
- Doctor can choose only **Manage queue** and **Confirm clinic fee** permissions.
- **Copy code** places the exact 32-character, case-sensitive code on the clipboard.
- Assistant activation returns a `DLO-AST` identity and saves a restorable session.
- Assistant sees only the assigned clinic.
- Assistant never sees Doctor profile editing, onboarding, announcements, Assistant management, or schedule management.
- Queue and clinic-fee controls are enabled only when their individual hosted permissions are granted.
- Doctor can later disable the Assistant or change either permission and the API enforces the new state.
- Local prototype Assistant records are never uploaded.

Run after API migration 062 and the Patient/Admin Stage 63P-D builds are live.
