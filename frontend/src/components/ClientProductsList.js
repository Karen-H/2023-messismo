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
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 15px;
    padding: 10px;
  }
  
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 5px;
  }
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
  
  @media (max-width: 768px) {
    padding: 15px;
    border-radius: 10px;
  }
  
  @media (max-width: 480px) {
    padding: 12px;
    border-radius: 8px;
  }
`;

const ProductName = styled.h3`
  color: #053271;
  margin-bottom: 10px;
  font-size: 1.4rem;
  
  @media (max-width: 768px) {
    font-size: 1.2rem;
    margin-bottom: 8px;
  }
  
  @media (max-width: 480px) {
    font-size: 1.1rem;
    margin-bottom: 6px;
  }
`;

const ProductCategory = styled.div`
  background: #a4d4cc;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  display: inline-block;
  font-size: 0.9rem;
  margin-bottom: 15px;
  
  @media (max-width: 768px) {
    font-size: 0.8rem;
    padding: 4px 12px;
    margin-bottom: 12px;
  }
  
  @media (max-width: 480px) {
    font-size: 0.75rem;
    padding: 3px 10px;
    margin-bottom: 10px;
  }
`;

const ProductDescription = styled.p`
  color: #666;
  margin: 15px 0;
  line-height: 1.4;
  
  @media (max-width: 768px) {
    font-size: 0.9rem;
    margin: 12px 0;
    line-height: 1.3;
  }
  
  @media (max-width: 480px) {
    font-size: 0.85rem;
    margin: 10px 0;
    line-height: 1.2;
  }
`;

const ProductPrice = styled.div`
  font-size: 1.5rem;
  font-weight: bold;
  color: #053271;
  text-align: right;
  
  @media (max-width: 768px) {
    font-size: 1.3rem;
  }
  
  @media (max-width: 480px) {
    font-size: 1.2rem;
    text-align: center;
  }
`;

const Title = styled.h2`
  color: #053271;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.5rem;
  
  @media (max-width: 768px) {
    font-size: 2rem;
    margin-bottom: 20px;
  }
  
  @media (max-width: 480px) {
    font-size: 1.5rem;
    margin-bottom: 15px;
  }
`;

const NoProductsMessage = styled.div`
  text-align: center;
  color: #666;
  font-size: 1.2rem;
  grid-column: 1 / -1;
  padding: 20px;
  
  @media (max-width: 768px) {
    font-size: 1rem;
    padding: 15px;
  }
  
  @media (max-width: 480px) {
    font-size: 0.9rem;
    padding: 10px;
  }
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