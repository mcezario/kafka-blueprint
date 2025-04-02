from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework import status
from .models import Tenant
from .serializers import TenantSerializer

class TenantViewSet(viewsets.ModelViewSet):
    queryset = Tenant.objects.all()
    serializer_class = TenantSerializer

    def create(self, request, *args, **kwargs):
        """POST endpoint for creating a tenant"""
        return super().create(request, *args, **kwargs)

    def list(self, request, *args, **kwargs):
        """GET endpoint to retrieve all tenants"""
        tenants = self.get_queryset()
        serializer = self.get_serializer(tenants, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def retrieve(self, request, pk=None):
        """GET endpoint to retrieve a single tenant by ID"""
        try:
            tenant = Tenant.objects.get(pk=pk)
            serializer = self.get_serializer(tenant)
            return Response(serializer.data, status=status.HTTP_200_OK)
        except Tenant.DoesNotExist:
            return Response({"error": "Tenant not found"}, status=status.HTTP_404_NOT_FOUND)