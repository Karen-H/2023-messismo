import React, { useState, useEffect } from "react";
import styled from "styled-components";
import { useSelector } from "react-redux";
import Navbar from "../components/Navbar";
import { Navigate } from "react-router-dom";
import CircularProgress from '@mui/material/CircularProgress';
import { Box, Typography } from '@mui/material';

const Container = styled.div`
  display: flex;
  flex-direction: column;
`;

const MainContent = styled.div`
  display: ${(props) => (props.visible ? "" : "none")};
  width: 100%;
  margin: auto;
  padding: 1rem;

  @media (min-width: 768px) {
    width: 80%;
  }
`;

const SettingsCard = styled.div`
  background: white;
  border-radius: 10px;
  padding: 2rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
`;

const Title = styled.h1`
  color: #333;
  margin-bottom: 2rem;
  font-family: "Roboto", sans-serif;
`;

const SettingTitle = styled.h2`
  color: #555;
  margin-bottom: 1rem;
  font-family: "Roboto", sans-serif;
  font-size: 1.5rem;
`;

const SettingDescription = styled.p`
  color: #666;
  margin-bottom: 1.5rem;
  font-family: "Roboto", sans-serif;
`;

const InputGroup = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
`;

const Label = styled.label`
  font-family: "Roboto", sans-serif;
  font-weight: 500;
  color: #333;
  min-width: 100px;
`;

const Input = styled.input`
  padding: 0.75rem;
  border: 2px solid #ddd;
  border-radius: 5px;
  font-size: 1rem;
  width: 200px;
  
  &:focus {
    outline: none;
    border-color: #007bff;
  }
`;

const Button = styled.button`
  background: #007bff;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
  font-family: "Roboto", sans-serif;
  
  &:hover {
    background: #0056b3;
  }
  
  &:disabled {
    background: #ccc;
    cursor: not-allowed;
  }
`;

const Message = styled.div`
  padding: 1rem;
  border-radius: 5px;
  margin-bottom: 1rem;
  font-family: "Roboto", sans-serif;
  
  ${props => props.type === 'success' && `
    background: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
  `}
  
  ${props => props.type === 'error' && `
    background: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
  `}
`;

const CurrentValue = styled.div`
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 5px;
  margin-bottom: 1rem;
  font-family: "Roboto", sans-serif;
  
  strong {
    color: #007bff;
    font-size: 1.2rem;
  }
`;

const Settings = () => {
  const { user: currentUser } = useSelector((state) => state.auth);
  const [visible, setVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [currentRate, setCurrentRate] = useState(100);
  const [newRate, setNewRate] = useState("");
  const [message, setMessage] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    setVisible(true);
    if (currentUser && currentUser.access_token) {
      fetchCurrentRate();
    } else {
      setLoading(false);
    }
  }, [currentUser]);

  const fetchCurrentRate = async () => {
    if (!currentUser || !currentUser.access_token) {
      setLoading(false);
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/settings/points-conversion", {
        headers: {
          "Authorization": "Bearer " + currentUser.access_token,
          "Content-Type": "application/json"
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setCurrentRate(data.conversionRate);
        setNewRate(data.conversionRate.toString());
      } else {
        
        // Usar valores por defecto si no se puede cargar
        setCurrentRate(100);
        setNewRate("100");
        setMessage({ 
          type: 'error', 
          text: `Error ${response.status}: ${response.statusText}. Mostrando valores por defecto.` 
        });
      }
    } catch (error) {
      console.error("Error fetching current rate:", error);
      // Usar valores por defecto si hay error de conexión
      setCurrentRate(100);
      setNewRate("100");
      setMessage({ type: 'error', text: 'Error de conexión al servidor. Mostrando valores por defecto.' });
    } finally {
      setLoading(false);
    }
  };

  const updateRate = async () => {
    const rate = parseFloat(newRate);
    
    if (!rate || rate <= 0) {
      setMessage({ type: 'error', text: 'Por favor ingresa un valor válido mayor a 0' });
      return;
    }

    setUpdating(true);
    setMessage(null);

    try {
      const response = await fetch("http://localhost:8080/settings/points-conversion", {
        method: 'PUT',
        headers: {
          "Authorization": "Bearer " + currentUser.access_token,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          conversionRate: rate
        })
      });

      if (response.ok) {
        setCurrentRate(rate);
        setMessage({ 
          type: 'success', 
          text: `Tasa de conversión actualizada correctamente a $${rate} = 1 punto` 
        });
      } else {
        setMessage({ type: 'error', text: 'Error al actualizar la configuración' });
      }
    } catch (error) {
      console.error("Error updating rate:", error);
      setMessage({ type: 'error', text: 'Error de conexión al servidor' });
    } finally {
      setUpdating(false);
    }
  };

  const handleInputChange = (e) => {
    const value = e.target.value;
    // Solo permitir números y un punto decimal
    if (/^\d*\.?\d*$/.test(value)) {
      setNewRate(value);
    }
  };

  if (!currentUser) {
    return <Navigate to="/welcome" />;
  }

  // Verificar roles - el usuario puede tener 'role' (singular) o 'roles' (plural)
  const userRole = currentUser.role || (currentUser.roles && currentUser.roles[0]);
  const isAdminOrManager = userRole === "ADMIN" || userRole === "MANAGER" || 
                          userRole === "ROLE_ADMIN" || userRole === "ROLE_MANAGER" ||
                          (currentUser.roles && currentUser.roles.includes("ROLE_ADMIN")) ||
                          (currentUser.roles && currentUser.roles.includes("ROLE_MANAGER"));

  if (!isAdminOrManager) {
    return <Navigate to="/homepage" />;
  }

  if (loading) {
    return (
      <Container>
        <Navbar />
        <MainContent visible={true}>
          <Box display="flex" justifyContent="center" mt={4}>
            <CircularProgress />
          </Box>
        </MainContent>
      </Container>
    );
  }

  return (
    <Container>
      <Navbar />
      <MainContent visible={visible}>
        <Box sx={{ width: '100%', margin: 'auto', padding: '1rem' }}>
          <Typography variant="h4" component="h1" sx={{ mb: 3, color: '#333', fontFamily: 'Roboto' }}>
            Configuración del Sistema
          </Typography>
        
        <SettingsCard>
          <SettingTitle>Conversión de Puntos de Fidelidad</SettingTitle>
          <SettingDescription>
            Configura la cantidad de pesos necesaria para que un cliente obtenga 1 punto de fidelidad.
          </SettingDescription>
          
          <CurrentValue>
            <strong>Configuración actual: ${currentRate} = 1 punto</strong>
            <div style={{ marginTop: '0.5rem', fontSize: '0.9rem', color: '#666' }}>
              Ejemplo: Una orden de $500 = {(500 / currentRate).toFixed(2)} puntos
            </div>
          </CurrentValue>

          {message && (
            <Message type={message.type}>
              {message.text}
            </Message>
          )}

          <InputGroup>
            <Label>Nueva tasa:</Label>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span>$</span>
              <Input
                type="text"
                value={newRate}
                onChange={handleInputChange}
                placeholder="100"
                disabled={updating}
              />
              <span>= 1 punto</span>
            </div>
          </InputGroup>

          <Button onClick={updateRate} disabled={updating || !newRate}>
            {updating ? 'Actualizando...' : 'Actualizar Configuración'}
          </Button>
        </SettingsCard>

        <SettingsCard>
          <SettingTitle>Historial de Configuraciones</SettingTitle>
          <SettingDescription>
            Cambios recientes en la configuración del sistema.
          </SettingDescription>
          
          <div style={{ marginTop: '1rem' }}>
            <div style={{ 
              padding: '0.75rem', 
              borderLeft: '4px solid #007bff', 
              backgroundColor: '#f8f9fa', 
              marginBottom: '0.5rem' 
            }}>
              <div style={{ fontWeight: 'bold', fontSize: '0.9rem', color: '#333' }}>
                Configuración Actual
              </div>
              <div style={{ fontSize: '0.8rem', color: '#666', marginTop: '0.25rem' }}>
                Tasa: ${currentRate} = 1 punto | Configurado al inicializar el sistema
              </div>
            </div>
            
            <div style={{ 
              padding: '0.75rem', 
              borderLeft: '4px solid #28a745', 
              backgroundColor: '#f8f9fa', 
              marginBottom: '0.5rem',
              opacity: message?.type === 'success' ? 1 : 0.5
            }}>
              <div style={{ fontWeight: 'bold', fontSize: '0.9rem', color: '#333' }}>
                {message?.type === 'success' ? 'Último Cambio' : 'Sin Cambios Recientes'}
              </div>
              <div style={{ fontSize: '0.8rem', color: '#666', marginTop: '0.25rem' }}>
                {message?.type === 'success' 
                  ? `Nueva tasa configurada: $${newRate} = 1 punto`
                  : 'No se han realizado cambios en esta sesión'
                }
              </div>
            </div>
          </div>
        </SettingsCard>
        </Box>
      </MainContent>
    </Container>
  );
};

export default Settings;