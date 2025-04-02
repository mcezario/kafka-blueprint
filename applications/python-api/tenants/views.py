from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view
from .models import Tenant
from .serializers import TenantSerializer

@api_view(['POST'])
def create_tenant(request):
    serializer = TenantSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)