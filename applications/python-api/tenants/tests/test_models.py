import pytest
from tenants.models import Tenant


@pytest.mark.django_db
def test_tenant_creation():
    tenant = Tenant.objects.create(name="Test Tenant")

    assert tenant.name == "Test Tenant"
