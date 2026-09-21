from app.config import settings


def test_default_configuration_is_not_production_ready():
    assert not settings.production_ready
    assert 'GOOGLE_CLIENT_ID' in settings.missing_production_config
    assert 'GOOGLE_REFRESH_TOKEN' in settings.missing_production_config
