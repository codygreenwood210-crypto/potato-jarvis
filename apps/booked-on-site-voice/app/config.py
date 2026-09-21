from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import List


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file='.env', env_file_encoding='utf-8', extra='ignore')

    environment: str = 'test'
    public_base_url: str = 'https://example.invalid'
    webhook_bearer_token: str = 'dev-token'
    booking_secret: str = 'replace-me'

    business_name: str = 'Booked On Site Demo Plumbing'
    timezone: str = 'Australia/Melbourne'
    service_areas: str = 'Melbourne CBD,Richmond,South Yarra,Carlton,Fitzroy'
    allowed_services: str = 'blocked drain,leaking tap,hot water,burst pipe,toilet repair'
    human_escalation_phone: str = ''

    google_client_id: str = ''
    google_client_secret: str = ''
    google_refresh_token: str = ''
    google_calendar_id: str = 'primary'
    notification_email: str = 'bookedonsite@gmail.com'
    gmail_sender: str = 'bookedonsite@gmail.com'

    @property
    def service_area_list(self) -> List[str]:
        return [x.strip().lower() for x in self.service_areas.split(',') if x.strip()]

    @property
    def allowed_service_list(self) -> List[str]:
        return [x.strip().lower() for x in self.allowed_services.split(',') if x.strip()]

    @property
    def missing_production_config(self) -> list[str]:
        missing: list[str] = []
        if not self.webhook_bearer_token or self.webhook_bearer_token == 'dev-token':
            missing.append('WEBHOOK_BEARER_TOKEN')
        if not self.booking_secret or self.booking_secret == 'replace-me':
            missing.append('BOOKING_SECRET')
        if not self.google_client_id:
            missing.append('GOOGLE_CLIENT_ID')
        if not self.google_client_secret:
            missing.append('GOOGLE_CLIENT_SECRET')
        if not self.google_refresh_token:
            missing.append('GOOGLE_REFRESH_TOKEN')
        if not self.human_escalation_phone:
            missing.append('HUMAN_ESCALATION_PHONE')
        return missing

    @property
    def production_ready(self) -> bool:
        return not self.missing_production_config


settings = Settings()
