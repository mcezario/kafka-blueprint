from django.db import migrations, models


class Migration(migrations.Migration):
    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Tenant',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=255)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
            ],
        ),
        migrations.RunSQL(
            sql="ALTER TABLE tenants_tenant REPLICA IDENTITY FULL;",
            reverse_sql="ALTER TABLE tenants_tenant REPLICA IDENTITY DEFAULT;"
        )
    ]
