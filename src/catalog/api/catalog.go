// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package api

import (
	"context"

	"github.com/aws-containers/retail-store-sample-app/catalog/model"
	"github.com/aws-containers/retail-store-sample-app/catalog/repository"
)

// CatalogAPI type
type CatalogAPI struct {
	repository repository.CatalogRepository
}

func (a *CatalogAPI) GetProducts(tags []string, order string, pageNum, pageSize int, ctx context.Context) ([]model.Product, error) {
	products, err := a.repository.GetProducts(tags, order, pageNum, pageSize, ctx)
	if err != nil {
		return nil, err
	}
	return products, nil
}

func (a *CatalogAPI) GetProduct(id string, ctx context.Context) (*model.Product, error) {
	return a.repository.GetProduct(id, ctx)
}

func (a *CatalogAPI) GetTags(ctx context.Context) ([]model.Tag, error) {
	return a.repository.GetTags(ctx)
}

func (a *CatalogAPI) GetSize(tags []string, ctx context.Context) (int, error) {
	return a.repository.CountProducts(tags, ctx)
}

// CreateProduct - admin only, enforced at the route/middleware level.
func (a *CatalogAPI) CreateProduct(product model.Product, ctx context.Context) (*model.Product, error) {
	return a.repository.CreateProduct(product, ctx)
}

// DeleteProduct - admin only, enforced at the route/middleware level.
func (a *CatalogAPI) DeleteProduct(id string, ctx context.Context) error {
	return a.repository.DeleteProduct(id, ctx)
}

// NewCatalogAPI constructor
func NewCatalogAPI(repository repository.CatalogRepository) (*CatalogAPI, error) {
	return &CatalogAPI{
		repository: repository,
	}, nil
}
