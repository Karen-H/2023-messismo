import React, { useState, useEffect } from "react";
import styled from "styled-components";
import clientService from "../services/client.service";
import ordersService from "../services/orders.service";
import benefitsService from "../services/benefits.service";

const Container = styled.div`
  background-color: black;
  padding: 2rem;
  border-radius: 8px;
  min-width: 400px;
  max-width: 500px;
  border: none;
  outline: none;
`;

const Title = styled.h2`
  color: white;
  font-family: "Roboto";
  text-align: center;
  margin-bottom: 2rem;
`;

const FormGroup = styled.div`
  margin-bottom: 1.5rem;
`;

const Label = styled.label`
  color: white;
  font-family: "Roboto";
  font-size: 1.1rem;
  display: block;
  margin-bottom: 0.5rem;
`;

const Select = styled.select`
  width: 100%;
  padding: 0.8rem;
  font-size: 1rem;
  border: 1px solid #a4d4cc;
  border-radius: 4px;
  background-color: white;
  font-family: "Roboto";

  &:focus {
    outline: none;
    border-color: #7cb9b0;
  }
`;

const ButtonContainer = styled.div`
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
`;

const Button = styled.button`
  font-size: 1.1rem;
  border-radius: 3px;
  padding: 0.8rem 2rem;
  border: 1px solid black;
  background-color: ${props => props.primary ? '#a4d4cc' : '#ccc'};
  color: black;
  text-transform: uppercase;
  cursor: pointer;
  letter-spacing: 1px;
  font-family: "Roboto", serif;
  text-align: center;

  &:hover {
    background-color: ${props => props.primary ? '#a7d0cd' : '#bbb'};
  }

  &:active {
    box-shadow: 0 2px #666;
    transform: translateY(2px);
  }

  &:focus {
    outline: none;
  }

  &:disabled {
    background-color: #666;
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

const ErrorMessage = styled.div`
  color: #f44336;
  font-size: 0.9rem;
  margin-top: 0.5rem;
  text-align: center;
  font-weight: bold;
`;

const SuccessMessage = styled.div`
  color: #51cf66;
  font-size: 0.9rem;
  margin-top: 0.5rem;
  text-align: center;
`;

function CloseOrderForm({ orderId, onCancel, onSuccess }) {
  
  const [clients, setClients] = useState([]);
  const [selectedClientId, setSelectedClientId] = useState("");
  const [availableBenefits, setAvailableBenefits] = useState([]);
  const [selectedBenefitId, setSelectedBenefitId] = useState("");
  const [clientPoints, setClientPoints] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Función para cargar clientes
  const loadClients = () => {
    clientService.getAllClients()
      .then((response) => {
        setClients(response.data);
      })
      .catch((error) => {
        console.error("Error al cargar clientes:", error);
        setError("Error al cargar la lista de clientes");
      });
  };

  useEffect(() => {
    loadClients();
  }, []);

  const handleClientChange = (clientId) => {
    setSelectedClientId(clientId);
    setSelectedBenefitId("");
    setAvailableBenefits([]);
    
    if (clientId) {
      // Encontrar el cliente seleccionado para obtener sus puntos
      const selectedClient = clients.find(client => client.clientId === clientId);
      if (selectedClient) {
        setClientPoints(selectedClient.currentPoints || 0);
        
        // Cargar beneficios disponibles para este cliente
        if (selectedClient.currentPoints > 0) {
          benefitsService.getBenefitsForPoints(selectedClient.currentPoints)
            .then((response) => {
              setAvailableBenefits(response.data);
            })
            .catch((error) => {
              console.error("Error al cargar beneficios:", error);
            });
        }
      }
    } else {
      setClientPoints(0);
    }
  };

  const handleSubmit = async (e) => {
    if (e && e.preventDefault) {
      e.preventDefault();
    }

    setError("");
    setSuccess("");
    setIsLoading(true);

    if (!selectedClientId) {
      setError("Por favor selecciona un cliente");
      setIsLoading(false);
      return;
    }

    try {
      const closeOrderData = {
        orderId: orderId,
        clientId: parseInt(selectedClientId)
      };
      
      if (selectedBenefitId) {
        closeOrderData.benefitId = parseInt(selectedBenefitId);
      }

      
      const response = await ordersService.closeOrderWithClient(closeOrderData);
      
      onSuccess();
    } catch (error) {
      console.error("Error completo al cerrar la orden:", error);
      console.error("Response data:", error.response?.data);
      console.error("Response status:", error.response?.status);
      
      if (error.response && error.response.data) {
        if (typeof error.response.data === 'string') {
          setError(error.response.data);
        } else if (error.response.data.message) {
          setError(error.response.data.message);
        } else {
          setError("Error desconocido del servidor");
        }
      } else {
        setError("Error al cerrar la orden. Inténtalo de nuevo.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    onCancel();
  };

  return (
    <Container>
      <Title>Close Order #{orderId}</Title>
      <div>
        <FormGroup>
          <Label htmlFor="clientId">Assign Client:</Label>
          <Select
            id="clientId"
            value={selectedClientId}
            onChange={(e) => handleClientChange(e.target.value)}
            disabled={isLoading}
          >
            <option value="">Select client...</option>
            {clients.map((client) => (
              <option key={client.clientId} value={client.clientId}>
                {client.username} ({client.email}) - Points: {client.currentPoints || 0}
              </option>
            ))}
          </Select>
        </FormGroup>

        {selectedClientId && (
          <FormGroup>
            <Label htmlFor="benefitId">Apply Benefit (Optional):</Label>
            <div style={{color: '#999', fontSize: '0.85rem', marginBottom: '0.5rem'}}>
              Only benefits valid for today are shown
            </div>
            <Select
              id="benefitId"
              value={selectedBenefitId}
              onChange={(e) => setSelectedBenefitId(e.target.value)}
              disabled={isLoading}
            >
              <option value="">No benefit</option>
              {availableBenefits.map((benefit) => {
                let displayText = `${benefit.pointsRequired} points`;
                
                if (benefit.type === 'DISCOUNT') {
                  if (benefit.discountType === 'PERCENTAGE') {
                    displayText += ` - ${benefit.discountValue}% OFF`;
                  } else {
                    displayText += ` - $${benefit.discountValue} OFF`;
                  }
                } else if (benefit.type === 'FREE_PRODUCT') {
                  const productName = benefit.productNames && benefit.productNames.length > 0 
                    ? benefit.productNames[0] 
                    : 'Product';
                  displayText += ` - Free ${productName}`;
                }
                
                return (
                  <option key={benefit.id} value={benefit.id}>
                    {displayText}
                  </option>
                );
              })}
            </Select>
            {availableBenefits.length === 0 && selectedClientId && (
              <div style={{color: '#f44336', fontSize: '0.9rem', marginTop: '0.5rem', fontWeight: 'bold'}}>
                No benefits available for this client
              </div>
            )}
          </FormGroup>
        )}

        {error && <ErrorMessage>{error}</ErrorMessage>}

        <ButtonContainer>
          <Button 
            type="button" 
            onClick={handleCancel}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button 
            type="button" 
            primary 
            disabled={isLoading || !selectedClientId}
            onClick={handleSubmit}
          >
            {isLoading ? "Closing..." : "Close Order"}
          </Button>
        </ButtonContainer>
      </div>
    </Container>
  );
}

export default CloseOrderForm;