import pytest
from tenants.serializers import TenantSerializer


def test_valid_tenant_serializer():
    valid_data = {"name": "Tenant 1"}
    serializer = TenantSerializer(data=valid_data)

    assert serializer.is_valid()
    assert serializer.validated_data["name"] == "Tenant 1"


def test_invalid_tenant_serializer():
    invalid_data = {"name": ""}
    serializer = TenantSerializer(data=invalid_data)

    assert not serializer.is_valid()
    assert "name" in serializer.errors
