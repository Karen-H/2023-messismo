import React, { useState, useEffect } from "react";
import styled from "styled-components";
import { useSelector } from "react-redux";
import { Navigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import benefitsService from "../services/benefits.service";
import clientService from "../services/client.service";
import productsService from "../services/products.service";
import { CircularProgress, Typography, Button, Card, CardContent, Chip, Dialog, DialogTitle, DialogContent, DialogActions } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { MdCardGiftcard, MdDiscount, MdDeleteForever } from "react-icons/md";
import moment from "moment";
import BenefitForm from "../components/BenefitForm";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  height: 100vh;
`;

const MainContent = styled.div`
  display: ${(props) => (props.visible ? "block" : "none")};
  padding: 2rem;
  
  @media (max-width: 768px) {
    padding: 1rem;
  }
`;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
`;



const StatsContainer = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
`;

const StatCard = styled(Card)`
  && {
    background: linear-gradient(135deg, #a4d4cc 0%, #7bb3a8 100%);
    color: white;
    text-align: center;
    
    .MuiCardContent-root {
      padding: 1.5rem;
    }
  }
`;

const BenefitsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
  margin-top: 2rem;
`;

const BenefitCard = styled(Card)`
  && {
    border-left: 4px solid ${props => 
      props.type === 'DISCOUNT' ? '#ff9800' : 
      props.type === 'FREE_PRODUCT' ? '#4caf50' : 
      '#a4d4cc'
    };
    transition: transform 0.2s, box-shadow 0.2s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }
  }
`;

const BenefitHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
`;

const BenefitIcon = styled.div`
  font-size: 2rem;
  color: ${props => 
    props.type === 'DISCOUNT' ? '#ff9800' : 
    props.type === 'FREE_PRODUCT' ? '#4caf50' : 
    '#a4d4cc'
  };
`;

const Points = styled.div`
  background: ${props => 
    props.type === 'DISCOUNT' ? '#ff9800' : 
    props.type === 'FREE_PRODUCT' ? '#4caf50' : 
    '#a4d4cc'
  };
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: bold;
`;

const CreateButton = styled.button`
  display: inline-block;
  font-size: 1.2rem;
  border-radius: 3px;
  padding: 1rem 3.5rem;
  margin-top: 3rem;
  border: 1px solid black;
  background-color: #a4d4cc;
  color: black;
  text-transform: uppercase;
  cursor: pointer;
  letter-spacing: 1px;
  box-shadow: 0 3px #999;
  font-family: "Roboto", serif;
  text-align: center;

  &:hover {
    background-color: #a7d0cd;
  }
  &:active {
    background-color: #a4d4cc;
    box-shadow: 0 3px #666;
    transform: translateY(4px);
  }
  &:focus {
    outline: none;
  }

  @media (max-width: 477px) {
    margin-top: 1rem;
    font-size: 1rem;
    padding: 1rem 2.5rem;
  }

  &:hover {
    background-color: #a7d0cd;
  }
  &:active {
    background-color: #a4d4cc;
    box-shadow: 0 3px #666;
    transform: translateY(4px);
  }
  &:focus {
    outline: none;
  }

  @media (max-width: 477px) {
    margin-top: 1rem;
    font-size: 1rem;
    padding: 1rem 2.5rem;
  }
`;

function Benefits() {
  const { user: currentUser } = useSelector((state) => state.auth);
  const clicked = useSelector((state) => state.navigation.clicked);
  
  const [benefits, setBenefits] = useState([]);
  const [myPoints, setMyPoints] = useState(0);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [selectedType, setSelectedType] = useState('ALL');

  const isAdminOrManager = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'MANAGER');
  const contentVisible = !clicked;

  useEffect(() => {
    loadBenefits();
    loadProducts();
    if (currentUser.role === 'CLIENT') {
      loadMyPoints();
    }
  }, []);

  const loadBenefits = async () => {
    try {
      const response = await benefitsService.getAllBenefits();
      setBenefits(response.data);
    } catch (error) {
      console.error("Error loading benefits:", error);
    } finally {
      setLoading(false);
    }
  };

  const loadMyPoints = async () => {
    try {
      const response = await clientService.getProfile();
      setMyPoints(response.data.currentPoints || 0);
    } catch (error) {
      console.error("Error loading points:", error);
    }
  };

  const loadProducts = async () => {
    try {
      const response = currentUser.role === 'CLIENT' 
        ? await productsService.getProductsForClient()
        : await productsService.getAllProducts();
      setProducts(response.data);
    } catch (error) {
      console.error("Error loading products:", error);
    }
  };



  const handleDeleteBenefit = async (benefitId) => {
    if (window.confirm("Are you sure you want to delete this benefit?")) {
      try {
        await benefitsService.deleteBenefit(benefitId);
        loadBenefits();
      } catch (error) {
        console.error("Error deleting benefit:", error);
      }
    }
  };

  const handleCreateBenefit = () => {
    setOpenCreateDialog(false);
    loadBenefits();
  };

  const filteredBenefits = benefits.filter(benefit => {
    if (selectedType === 'ALL') return true;
    return benefit.type === selectedType;
  });

  const availableBenefits = filteredBenefits.filter(benefit => 
    currentUser.role === 'CLIENT' ? benefit.pointsRequired <= myPoints : true
  );

  const discountBenefits = benefits.filter(b => b.type === 'DISCOUNT').length;
  const freeProductBenefits = benefits.filter(b => b.type === 'FREE_PRODUCT').length;

  if (!currentUser) {
    return <Navigate to="/" />;
  }

  if (loading) {
    return (
      <Container>
        <Navbar />
        <MainContent visible={contentVisible}>
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
            <CircularProgress />
          </div>
        </MainContent>
      </Container>
    );
  }

  return (
    <Container>
      <Navbar />
      <MainContent visible={contentVisible}>
        {isAdminOrManager && (
          <CreateButton onClick={() => setOpenCreateDialog(true)}>
            Create Benefit
          </CreateButton>
        )}

        <div style={{ marginTop: '2rem', marginBottom: '2rem' }}>
          <Typography
            variant="h3"
            component="h3"
            sx={{ textAlign: "center", mt: 3, mb: 1, color: "white" }}
          >
            Benefits
          </Typography>
        </div>

        {/* Stats Cards */}
        <StatsContainer style={{ marginBottom: '2rem' }}>
          {currentUser.role === 'CLIENT' && (
            <StatCard>
              <CardContent>
                <Typography variant="h4" component="div" style={{ fontWeight: 'bold' }}>
                  {myPoints}
                </Typography>
                <Typography variant="h6">
                  My Points
                </Typography>
              </CardContent>
            </StatCard>
          )}
          
          <StatCard>
            <CardContent>
              <Typography variant="h4" component="div" style={{ fontWeight: 'bold' }}>
                {benefits.length}
              </Typography>
              <Typography variant="h6">
                Total Benefits
              </Typography>
            </CardContent>
          </StatCard>

          <StatCard>
            <CardContent>
              <Typography variant="h4" component="div" style={{ fontWeight: 'bold' }}>
                {discountBenefits}
              </Typography>
              <Typography variant="h6">
                Discounts
              </Typography>
            </CardContent>
          </StatCard>

          <StatCard>
            <CardContent>
              <Typography variant="h4" component="div" style={{ fontWeight: 'bold' }}>
                {freeProductBenefits}
              </Typography>
              <Typography variant="h6">
                Free Products
              </Typography>
            </CardContent>
          </StatCard>
        </StatsContainer>

        {/* Filter Buttons */}
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', justifyContent: 'center', marginTop: '1rem' }}>
          <Button 
            variant="contained"
            onClick={() => setSelectedType('ALL')}
            style={{ 
              backgroundColor: '#a4d4cc', 
              color: 'black',
              opacity: selectedType === 'ALL' ? 1 : 0.7,
              '&:hover': { backgroundColor: '#a7d0cd' }
            }}
          >
            All
          </Button>
          <Button 
            variant="contained"
            onClick={() => setSelectedType('DISCOUNT')}
            style={{ 
              backgroundColor: '#a4d4cc', 
              color: 'black',
              opacity: selectedType === 'DISCOUNT' ? 1 : 0.7,
              '&:hover': { backgroundColor: '#a7d0cd' }
            }}
          >
            Discounts
          </Button>
          <Button 
            variant="contained"
            onClick={() => setSelectedType('FREE_PRODUCT')}
            style={{ 
              backgroundColor: '#a4d4cc', 
              color: 'black',
              opacity: selectedType === 'FREE_PRODUCT' ? 1 : 0.7,
              '&:hover': { backgroundColor: '#a7d0cd' }
            }}
          >
            Free Products
          </Button>
        </div>

        {/* Benefits Grid */}
        <BenefitsGrid>
          {(currentUser.role === 'CLIENT' ? filteredBenefits : availableBenefits).map((benefit) => (
            <BenefitCard key={benefit.id} type={benefit.type}>
              <CardContent>
                <BenefitHeader>
                  <BenefitIcon type={benefit.type}>
                    {benefit.type === 'DISCOUNT' ? <MdDiscount /> : <MdCardGiftcard />}
                  </BenefitIcon>
                  <Points type={benefit.type}>{benefit.pointsRequired} pts</Points>
                </BenefitHeader>

                <Typography variant="h6" style={{ marginBottom: '1rem', color: '#333' }}>
                  {benefit.type === 'DISCOUNT' ? 'Discount' : 'Free Product'}
                </Typography>

                {benefit.type === 'DISCOUNT' && (
                  <div style={{ marginBottom: '1rem' }}>
                    <Typography variant="body1" style={{ fontWeight: 'bold', color: '#ff9800' }}>
                      {benefit.discountType === 'PERCENTAGE' ? `${benefit.discountValue}% OFF` : `$${benefit.discountValue} OFF`}
                    </Typography>
                    <div style={{ marginTop: '0.5rem' }}>
                      {benefit.applicableDays?.map((day, index) => (
                        <Chip 
                          key={index} 
                          label={day} 
                          size="small" 
                          style={{ margin: '0.2rem', backgroundColor: '#f0f0f0' }}
                        />
                      ))}
                    </div>
                  </div>
                )}

                {benefit.type === 'FREE_PRODUCT' && (
                  <div style={{ marginBottom: '1rem' }}>
                    <Typography variant="body1" style={{ fontWeight: 'bold', color: '#4caf50' }}>
                      {benefit.productIds && benefit.productIds.length > 0
                        ? `Product: ${products.find(p => p.productId == benefit.productIds[0])?.name || 'Unknown'}`
                        : 'Free Product'
                      }
                    </Typography>
                    <div style={{ marginTop: '0.5rem' }}>
                      {benefit.applicableDays?.map((day, index) => (
                        <Chip 
                          key={index} 
                          label={day} 
                          size="small" 
                          style={{ margin: '0.2rem', backgroundColor: '#f0f0f0' }}
                        />
                      ))}
                    </div>
                  </div>
                )}

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem' }}>
                  <Typography variant="caption" style={{ color: '#666' }}>
                    Created: {moment(benefit.createdAt).format('DD/MM/YYYY')}
                  </Typography>
                  
                  {isAdminOrManager && (
                    <Button
                      size="small"
                      color="error"
                      onClick={() => handleDeleteBenefit(benefit.id)}
                      startIcon={<MdDeleteForever />}
                    >
                      Delete
                    </Button>
                  )}
                </div>

                {currentUser.role === 'CLIENT' && (() => {
                  const today = moment().format('dddd').toUpperCase();
                  const isAvailableToday = !benefit.applicableDays || 
                                         benefit.applicableDays.length === 0 || 
                                         benefit.applicableDays.includes(today);
                  const hasInsufficientPoints = benefit.pointsRequired > myPoints;
                  
                  const messages = [];
                  
                  if (!isAvailableToday) {
                    messages.push("Not available today");
                  }
                  
                  if (hasInsufficientPoints) {
                    messages.push(`You need ${benefit.pointsRequired - myPoints} more points`);
                  }
                  
                  if (messages.length > 0) {
                    return (
                      <div>
                        {messages.map((message, index) => (
                          <Typography 
                            key={index}
                            variant="body2" 
                            style={{ 
                              color: '#f44336', 
                              marginTop: index === 0 ? '0.5rem' : '0.25rem',
                              fontStyle: 'italic'
                            }}
                          >
                            {message}
                          </Typography>
                        ))}
                      </div>
                    );
                  }
                  
                  return null;
                })()}
              </CardContent>
            </BenefitCard>
          ))}
        </BenefitsGrid>

        {(currentUser.role === 'CLIENT' ? filteredBenefits : availableBenefits).length === 0 && (
          <div style={{ textAlign: 'center', marginTop: '3rem' }}>
            <Typography variant="h6" sx={{ color: 'white' }}>
              {selectedType === 'ALL' 
                ? 'No benefits available' 
                : `No ${selectedType === 'DISCOUNT' ? 'Discount' : 'Free Product'} benefits available`
              }
            </Typography>
          </div>
        )}
      </MainContent>

      {/* Create Benefit Dialog */}
      <Dialog 
        open={openCreateDialog} 
        onClose={() => setOpenCreateDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogContent>
          <BenefitForm 
            onSubmit={handleCreateBenefit}
            onCancel={() => setOpenCreateDialog(false)}
          />
        </DialogContent>
      </Dialog>
    </Container>
  );
}

export default Benefits;