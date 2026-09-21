import base64
import requests
from .config import settings

TOKEN_URL = 'https://oauth2.googleapis.com/token'
CAL_BASE = 'https://www.googleapis.com/calendar/v3'
GMAIL_BASE = 'https://gmail.googleapis.com/gmail/v1'


class GoogleOAuth:
    def access_token(self) -> str:
        if not (settings.google_client_id and settings.google_client_secret and settings.google_refresh_token):
            raise RuntimeError('google oauth credentials not configured')
        r = requests.post(TOKEN_URL, data={
            'client_id': settings.google_client_id,
            'client_secret': settings.google_client_secret,
            'refresh_token': settings.google_refresh_token,
            'grant_type': 'refresh_token',
        }, timeout=10)
        r.raise_for_status()
        return r.json()['access_token']

    def headers(self):
        return {'Authorization': f'Bearer {self.access_token()}', 'Content-Type': 'application/json'}


oauth = GoogleOAuth()


class GoogleCalendar:
    def freebusy(self, start_iso: str, end_iso: str) -> list:
        body = {
            'timeMin': start_iso,
            'timeMax': end_iso,
            'timeZone': settings.timezone,
            'items': [{'id': settings.google_calendar_id}],
        }
        r = requests.post(f'{CAL_BASE}/freeBusy', headers=oauth.headers(), json=body, timeout=10)
        r.raise_for_status()
        return r.json()['calendars'][settings.google_calendar_id]['busy']

    def create_event(self, start_iso: str, end_iso: str, summary: str, description: str) -> dict:
        body = {
            'summary': summary,
            'description': description,
            'start': {'dateTime': start_iso, 'timeZone': settings.timezone},
            'end': {'dateTime': end_iso, 'timeZone': settings.timezone},
        }
        r = requests.post(
            f'{CAL_BASE}/calendars/{settings.google_calendar_id}/events',
            headers=oauth.headers(),
            json=body,
            timeout=10,
        )
        r.raise_for_status()
        return r.json()


calendar = GoogleCalendar()


class GmailNotifier:
    def send(self, to: str, subject: str, body: str) -> dict:
        raw = (
            f'From: {settings.gmail_sender}\r\n'
            f'To: {to}\r\n'
            f'Subject: {subject}\r\n'
            'Content-Type: text/plain; charset=utf-8\r\n\r\n'
            f'{body}'
        )
        encoded = base64.urlsafe_b64encode(raw.encode('utf-8')).decode('ascii')
        r = requests.post(
            f'{GMAIL_BASE}/users/me/messages/send',
            headers=oauth.headers(),
            json={'raw': encoded},
            timeout=10,
        )
        r.raise_for_status()
        return r.json()


notifier = GmailNotifier()
