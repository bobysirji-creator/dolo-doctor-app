# Stage 18B Doctor communications device test

1. Install the new stable Doctor APK over the existing app and confirm local login, clinic and queue data remain intact.
2. Open Hosted staff queue, connect as seeded Doctor with PIN 1234, and confirm the announcement editor is visible.
3. Create an active `Camp` announcement using today's date for both start and end; confirm it remains after Refresh and app restart.
4. Edit its title/message, save, and confirm only the edited record remains.
5. Tap `Set draft`; confirm the Doctor list retains it as Draft.
6. Connect as seeded Assistant and confirm no hosted announcement editor is shown.
7. Restore the announcement to Published for the Patient App cross-check.
8. Disable network, retry a save, and confirm local Doctor data is unchanged; reconnect and retry successfully.
9. Confirm SMS, Push, Maps and Payments remain disabled.
