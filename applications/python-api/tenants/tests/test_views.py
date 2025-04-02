import pytest
from rest_framework.test import APIClient
from tenants.models import Tenant


@pytest.mark.django_db
def test_create_tenant_api():
    client = APIClient()
    data = {"name": "New Tenant"}

    response = client.post("/api/tenants/", data, format="json")

    assert response.status_code == 201
    assert Tenant.objects.count() == 1


@pytest.mark.django_db
def test_list_tenants_api():
    Tenant.objects.create(name="Tenant 1")
    Tenant.objects.create(name="Tenant 2")

    client = APIClient()
    response = client.get("/api/tenants/")

    assert response.status_code == 200
    assert len(response.json()) == 2
