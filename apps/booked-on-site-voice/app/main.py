from fastapi import FastAPI, Header, HTTPException, Request
from .config import settings
from .vapi import process_vapi_message

app = FastAPI(title='Booked On Site Voice Backend', version='0.2.0')


@app.get('/')
def root():
    return {
        'service': 'booked-on-site',
        'status': 'ready' if settings.production_ready else 'configuration-required',
        'health': '/health',
    }


@app.get('/health')
def health():
    return {
        'ok': True,
        'service': 'booked-on-site',
        'environment': settings.environment,
        'production_ready': settings.production_ready,
        'missing_configuration': settings.missing_production_config,
    }


@app.post('/vapi/webhook')
async def vapi_webhook(request: Request, authorization: str | None = Header(default=None)):
    if not settings.production_ready:
        raise HTTPException(status_code=503, detail='production configuration incomplete')
    expected = f'Bearer {settings.webhook_bearer_token}'
    if authorization != expected:
        raise HTTPException(status_code=401, detail='unauthorized')
    body = await request.json()
    return process_vapi_message(body)
