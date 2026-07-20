# Stages 16E and 16F Hosted Access Test

Use dummy data only. Platform API must deploy before installing the Doctor APK.

## Hosted API

1. Push Platform API and confirm GitHub Actions passes.
2. Confirm Render deploys `0.5.0-stage16e` and `/ready` reports `ready`.
3. Confirm capabilities report Stage `16.5`, transport `SERVER_MANAGED_STAFF_ACCESS`, Doctor synchronization enabled and all providers disabled.
4. Confirm OpenAPI contains `PUT /api/v1/staff/assistants/{assistantId}/access`.

## Android physical device

1. Install stable Doctor App `0.16.0-stage16f` over the accepted app without uninstalling. Confirm local login, clinic, assistants, queue, history and theme remain intact.
2. Login locally as Doctor, open **Hosted Stage 16F queue** (existing Home label), connect with hosted demo PIN `1234`, and find **Hosted Assistant access**.
3. Keep the Assistant active, disable **Confirm clinic fee**, save, then login to the hosted screen as the queue-enabled Assistant. Confirm queue controls remain available but fee admission is unavailable.
4. As Doctor, grant clinic-fee permission again and remove queue permission. Confirm the Assistant can admit a paid appointment but cannot start/pause/call or update the queue.
5. As Doctor, disable the hosted Assistant. Confirm the Assistant's existing hosted access fails on refresh and a new hosted connection cannot access the clinic.
6. Re-enable the Assistant with both permissions. Confirm hosted connection and both actions work again.
7. Restart the app and confirm the Doctor hosted session and server-owned Assistant state restore while online.
8. Disable connectivity during an update. Confirm an error appears, local data is unchanged, and refreshing after reconnection shows authoritative server state without duplicate mutations.
9. Disconnect hosted access and confirm local Doctor login and all local operational data remain unchanged.

Do not use real staff or patient details. The fixed demo PIN remains separate from local app PINs. SMS, Push, Maps and Payments remain disabled.
