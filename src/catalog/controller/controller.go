// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package controller

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/aws-containers/retail-store-sample-app/catalog/api"
	"github.com/aws-containers/retail-store-sample-app/catalog/httputil"
	"github.com/aws-containers/retail-store-sample-app/catalog/model"
	"github.com/gin-gonic/gin"
)

// Controller example
type Controller struct {
	api *api.CatalogAPI
}

// NewController example
func NewController(api *api.CatalogAPI) (*Controller, error) {
	return &Controller{
		api: api,
	}, nil
}

// GetProducts godoc
// @Router /catalog/products [get]
func (c *Controller) GetProducts(ctx *gin.Context) {
	var tags []string

	tagString := ctx.Query("tags")
	if len(tagString) > 0 {
		tags = strings.Split(tagString, ",")
	} else {
		tags = []string{}
	}

	order := ctx.Query("order")

	page, err := getQueryInt("page", 1, ctx)
	if err != nil {
		httputil.NewError(ctx, http.StatusBadRequest, err)
		return
	}

	size, err := getQueryInt("size", 10, ctx)
	if err != nil {
		httputil.NewError(ctx, http.StatusBadRequest, err)
		return
	}

	products, err := c.api.GetProducts(tags, order, page, size, ctx.Request.Context())
	if err != nil {
		httputil.NewError(ctx, http.StatusNotFound, err)
		return
	}
	ctx.JSON(http.StatusOK, products)
}

// GetProduct godoc
// @Router /catalog/products/{id} [get]
func (c *Controller) GetProduct(ctx *gin.Context) {
	id := ctx.Param("id")

	product, err := c.api.GetProduct(id, ctx.Request.Context())
	if err != nil {
		httputil.NewError(ctx, http.StatusNotFound, err)
		return
	}
	ctx.JSON(http.StatusOK, product)
}

// CatalogSize godoc
// @Router /catalog/size [get]
func (c *Controller) CatalogSize(ctx *gin.Context) {
	var tags []string

	tagString := ctx.Query("tags")
	if len(tagString) > 0 {
		tags = strings.Split(tagString, ",")
	} else {
		tags = []string{}
	}

	count, err := c.api.GetSize(tags, ctx.Request.Context())
	if err != nil {
		httputil.NewError(ctx, http.StatusNotFound, err)
		return
	}
	ctx.JSON(http.StatusOK, model.CatalogSizeResponse{
		Size: count,
	})
}

// ListTags godoc
// @Router /catalog/tags [get]
func (c *Controller) ListTags(ctx *gin.Context) {
	accounts, err := c.api.GetTags(ctx.Request.Context())
	if err != nil {
		httputil.NewError(ctx, http.StatusNotFound, err)
		return
	}
	ctx.JSON(http.StatusOK, accounts)
}

// CreateProduct godoc
// @Summary Create a product
// @Description Admin-only. Creates a new product; the ID is generated server-side.
// @Tags catalog
// @Accept json
// @Produce json
// @Param product body model.Product true "Product to create"
// @Success 201 {object} model.Product
// @Failure 400 {object} httputil.HTTPError
// @Failure 500 {object} httputil.HTTPError
// @Router /catalog/products [post]
func (c *Controller) CreateProduct(ctx *gin.Context) {
	var product model.Product

	if err := ctx.ShouldBindJSON(&product); err != nil {
		httputil.NewError(ctx, http.StatusBadRequest, err)
		return
	}

	created, err := c.api.CreateProduct(product, ctx.Request.Context())
	if err != nil {
		httputil.NewError(ctx, http.StatusInternalServerError, err)
		return
	}

	ctx.JSON(http.StatusCreated, created)
}

// DeleteProduct godoc
// @Summary Delete a product
// @Description Admin-only. Deletes a product by ID.
// @Tags catalog
// @Param id path string true "product ID"
// @Success 204
// @Failure 404 {object} httputil.HTTPError
// @Router /catalog/products/{id} [delete]
func (c *Controller) DeleteProduct(ctx *gin.Context) {
	id := ctx.Param("id")

	if err := c.api.DeleteProduct(id, ctx.Request.Context()); err != nil {
		httputil.NewError(ctx, http.StatusNotFound, err)
		return
	}

	ctx.Status(http.StatusNoContent)
}

func getQueryInt(name string, defaultValue int, ctx *gin.Context) (int, error) {
	str := ctx.Query(name)

	if len(str) > 0 {
		return strconv.Atoi(str)
	}

	return defaultValue, nil
}
