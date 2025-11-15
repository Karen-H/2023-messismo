import React, { useState, useEffect } from "react";
import styled from "styled-components";
import clientService from "../services/client.service";
import ordersService from "../services/orders.service";

const Container = styled.div`
  background-color: black;
  padding: 2rem;
  border-radius: 8px;
  min-width: 400px;
  max-width: 500px;
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
  color: #ff6b6b;
  font-size: 0.9rem;
  margin-top: 0.5rem;
  text-align: center;
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
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    // Cargar la lista de clientes
    clientService.getAllClients()
      .then((response) => {
        setClients(response.data);
      })
      .catch((error) => {
        console.error("Error al cargar clientes:", error);
        setError("Error al cargar la lista de clientes");
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setIsLoading(true);

    if (!selectedClientId) {
      setError("Por favor selecciona un cliente");
      setIsLoading(false);
      return;
    }

    try {
      await ordersService.closeOrderWithClient({
        orderId: orderId,
        clientId: parseInt(selectedClientId)
      });
      
      setSuccess("Orden cerrada exitosamente con cliente asignado");
      setTimeout(() => {
        onSuccess();
      }, 1500);
    } catch (error) {
      console.error("Error al cerrar la orden:", error);
      if (error.response && error.response.data && error.response.data.message) {
        setError(error.response.data.message);
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
      <Title>Cerrar Orden #{orderId}</Title>
      <form onSubmit={handleSubmit}>
        <FormGroup>
          <Label htmlFor="clientId">Asignar Cliente:</Label>
          <Select
            id="clientId"
            value={selectedClientId}
            onChange={(e) => setSelectedClientId(e.target.value)}
            disabled={isLoading}
          >
            <option value="">Seleccionar cliente...</option>
            {clients.map((client) => (
              <option key={client.clientId} value={client.clientId}>
                {client.username} ({client.email})
              </option>
            ))}
          </Select>
        </FormGroup>

        {error && <ErrorMessage>{error}</ErrorMessage>}
        {success && <SuccessMessage>{success}</SuccessMessage>}

        <ButtonContainer>
          <Button 
            type="button" 
            onClick={handleCancel}
            disabled={isLoading}
          >
            Cancelar
          </Button>
          <Button 
            type="submit" 
            primary 
            disabled={isLoading || !selectedClientId}
          >
            {isLoading ? "Cerrando..." : "Cerrar Orden"}
          </Button>
        </ButtonContainer>
      </form>
    </Container>
  );
}

export default CloseOrderForm;