import React, { useEffect, useState } from "react";
import styled from "styled-components";
import clientService from "../services/client.service";
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';

const ProductsContainer = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
  width: 100%;
  max-width: 1200px;
`;

const ProductCard = styled.div`
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  }
`;

const ProductName = styled.h3`
  color: #053271;
  margin-bottom: 10px;
  font-size: 1.4rem;
`;

const ProductCategory = styled.div`
  background: #a4d4cc;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  display: inline-block;
  font-size: 0.9rem;
  margin-bottom: 15px;
`;

const ProductDescription = styled.p`
  color: #666;
  margin: 15px 0;
  line-height: 1.4;
`;

const ProductPrice = styled.div`
  font-size: 1.5rem;
  font-weight: bold;
  color: #053271;
  text-align: right;
`;

const Title = styled.h2`
  color: #053271;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.5rem;
`;

const NoProductsMessage = styled.div`
  text-align: center;
  color: #666;
  font-size: 1.2rem;
  grid-column: 1 / -1;
`;

function ClientProductsList() {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    clientService.getProducts()
      .then((response) => {
        setProducts(response.data);
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("Error loading products:", error);
        setIsLoading(false);
      });
  }, []);

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: '20%' }}>
        <CircularProgress style={{ color: "#a4d4cc" }} />
      </Box>
    );
  }

  return (
    <div>
      <ProductsContainer>
        {products.length === 0 ? (
          <NoProductsMessage>No products available at the moment.</NoProductsMessage>
        ) : (
          products.map((product, index) => (
            <ProductCard key={index}>
              <ProductName>{product.name}</ProductName>
              <ProductCategory>{product.category}</ProductCategory>
              <ProductDescription>{product.description}</ProductDescription>
              <ProductPrice>${product.unitPrice.toFixed(2)}</ProductPrice>
            </ProductCard>
          ))
        )}
      </ProductsContainer>
    </div>
  );
}

export default ClientProductsList;