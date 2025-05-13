package controller

import (
    "net/http"

    "go-api/service"

    "github.com/gin-gonic/gin"
)

type CustomerRequest struct {
    ID   int64 `json:"id" binding:"required"`
    TenantID   int64 `json:"tenantId" binding:"required"`
    Name string `json:"name" binding:"required"`
}

func PostCustomer(c *gin.Context) {
    var req CustomerRequest
    if err := c.ShouldBindJSON(&req); err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
        return
    }

    err := service.ProduceCustomer(c.Request.Context(), req.ID, req.TenantID, req.Name)
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to produce message"})
        return
    }

    c.JSON(http.StatusOK, gin.H{"status": "message produced"})
}
