import React, { useState, useEffect } from "react";
import styled from "styled-components";
import { useSelector } from "react-redux";
import Navbar from "../components/Navbar";
import { Navigate } from "react-router-dom";
import CircularProgress from '@mui/material/CircularProgress';
import { Box, Typography } from '@mui/material';
import { DataGrid } from "@mui/x-data-grid";
import moment from "moment";

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
      fetchHistory();
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
          text: `Error ${response.status}: ${response.statusText}. Showing default values.` 
        });
      }
    } catch (error) {
      console.error("Error fetching current rate:", error);
      // Usar valores por defecto si hay error de conexión
      setCurrentRate(100);
      setNewRate("100");
      setMessage({ type: 'error', text: 'Server connection error. Showing default values.' });
    } finally {
      setLoading(false);
    }
  };

  const fetchHistory = async () => {
    if (!currentUser || !currentUser.access_token) {
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/settings/points_conversion_rate/history", {
        headers: {
          "Authorization": "Bearer " + currentUser.access_token,
          "Content-Type": "application/json"
        }
      });

      if (response.ok) {
        const historyData = await response.json();
        setHistory(historyData);
      }
    } catch (error) {
      console.error("Error fetching history:", error);
    }
  };

  const updateRate = async () => {
    const rate = parseFloat(newRate);
    
    if (!rate || rate <= 0) {
      setMessage({ type: 'error', text: 'Please enter a valid value greater than 0' });
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
        setMessage(null);
        // Recargar historial después de actualización exitosa
        fetchHistory();
      } else {
        setMessage({ type: 'error', text: 'Error updating configuration' });
      }
    } catch (error) {
      console.error("Error updating rate:", error);
      setMessage({ type: 'error', text: 'Server connection error' });
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

  // Configuración de columnas para la tabla de historial
  const historyColumns = [
    {
      field: "changedAt",
      headerName: "Date",
      flex: 1,
      align: "center",
      headerAlign: "center",
      minWidth: 150,
      renderCell: (params) =>
        moment(params.row.changedAt).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      field: "changedBy",
      headerName: "User",
      flex: 1,
      align: "center",
      headerAlign: "center",
      minWidth: 150,
    },
    {
      field: "oldValue",
      headerName: "Previous Value",
      flex: 1,
      align: "center",
      headerAlign: "center",
      minWidth: 120,
      renderCell: (params) => `$${params.value}`,
    },
    {
      field: "newValue",
      headerName: "New Value",
      flex: 1,
      align: "center",
      headerAlign: "center",
      minWidth: 120,
      renderCell: (params) => `$${params.value}`,
    },
  ];

  // Preparar datos para la tabla
  const historyRows = history.map((item, index) => ({
    id: index + 1,
    ...item
  }));

  return (
    <Container>
      <Navbar />
      <MainContent visible={visible}>
        <Typography
          variant="h3"
          component="h3"
          sx={{ textAlign: "center", mt: 3, mb: 3, color: "white" }}
        >
          Settings
        </Typography>
        
        <Box sx={{ width: "100%", mb: 3, mt: 6 }}>
          <Typography
            variant="h4"
            component="h4"
            sx={{ textAlign: "center", mb: 2, color: "#a4d4cc" }}
          >
            Loyalty Points Conversion
          </Typography>

          
          <Box sx={{ 
            textAlign: 'center', 
            mb: 6, 
            p: 2, 
            border: '2px solid #a4d4cc', 
            borderRadius: '8px',
            maxWidth: '400px',
            margin: '0 auto 48px auto'
          }}>
            <Typography variant="h6" sx={{ color: '#a4d4cc', fontWeight: 'bold' }}>
              Current setting: ${currentRate} = 1 point
            </Typography>
          </Box>

          {message && (
            <Box 
              sx={{ 
                p: 2, 
                mb: 2, 
                borderRadius: '8px',
                backgroundColor: message.type === 'success' ? '#28a745' : '#dc3545',
                color: 'white',
                textAlign: 'center'
              }}
            >
              {message.text}
            </Box>
          )}

          <Box sx={{ mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: '0.5rem', justifyContent: 'center' }}>
              <Typography sx={{ color: 'white' }}>$</Typography>
              <input
                type="text"
                value={newRate}
                onChange={handleInputChange}
                placeholder="100"
                disabled={updating}
                style={{
                  padding: '8px 12px',
                  borderRadius: '4px',
                  border: '2px solid #a4d4cc',
                  backgroundColor: 'transparent',
                  color: 'white',
                  fontSize: '1rem',
                  width: '100px',
                  textAlign: 'center'
                }}
              />
              <Typography sx={{ color: 'white' }}>= 1 point</Typography>
            </Box>
          </Box>

          <Box sx={{ textAlign: 'center', mb: 6 }}>
            <button
              onClick={updateRate} 
              disabled={updating || !newRate}
              style={{
                display: 'block',
                fontSize: '1.2rem',
                borderRadius: '3px',
                padding: '1rem 3.5rem',
                margin: 'auto',
                border: '1px solid black',
                backgroundColor: updating || !newRate ? '#666' : '#a4d4cc',
                color: 'black',
                textTransform: 'uppercase',
                cursor: updating || !newRate ? 'not-allowed' : 'pointer',
                letterSpacing: '1px',
                boxShadow: '0 3px #999',
                fontFamily: '"Roboto", serif',
                textAlign: 'center'
              }}
            >
              {updating ? 'Updating...' : 'UPDATE'}
            </button>
          </Box>
        </Box>

        <Box sx={{ width: "100%" }}>
          <Typography
            variant="h4"
            component="h4"
            sx={{ textAlign: "center", mb: 3, color: "#a4d4cc" }}
          >
            Conversion History
          </Typography>
          
          <Box sx={{ height: 400, width: "100%", mt: 2 }}>
            {history.length === 0 ? (
              <Box 
                display="flex" 
                justifyContent="center" 
                alignItems="center" 
                height="100%" 
                sx={{ 
                  border: '2px dashed #a4d4cc',
                  borderRadius: '8px',
                  padding: '2rem'
                }}
              >
                <Typography variant="body1" sx={{ color: 'white' }}>
                  No changes recorded
                </Typography>
              </Box>
            ) : (
              <DataGrid
                rows={historyRows}
                columns={historyColumns}
                initialState={{
                  pagination: { paginationModel: { pageSize: 5 } },
                  sorting: {
                    sortModel: [{ field: "changedAt", sort: "desc" }],
                  },
                }}
                pageSizeOptions={[5, 10, 25]}
                autoHeight={true}
                sx={{
                  fontSize: "1rem",
                  border: 2,
                  borderColor: "#a4d4cc",
                  "& .MuiButtonBase-root": {
                    color: "white",
                  },
                  "& .MuiDataGrid-cell:hover": {
                    color: "#a4d4cc",
                  },
                  ".MuiDataGrid-columnSeparator": {
                    display: "none",
                  },
                  color: "white",
                  fontFamily: "Roboto",
                  fontSize: "1.1rem",
                  ".MuiTablePagination-displayedRows": {
                    color: "white",
                    fontSize: "1.2rem",
                  },
                  ".MuiTablePagination-selectLabel": {
                    color: "white",
                    fontSize: "1.2rem",
                  },
                  "& .MuiSelect-select.MuiSelect-select": {
                    color: "white",
                    fontSize: "1.2rem",
                    marginTop: "0.7rem",
                  },
                  ".MuiDataGrid-sortIcon": {
                    opacity: "inherit !important",
                    color: "white",
                  },
                  "& .MuiDataGrid-cell:focus": {
                    outline: "none",
                  },
                  "@media (max-width: 1000px)": {
                    fontSize: "1rem",
                  },
                  "@media (max-width: 760px)": {
                    fontSize: "1rem",
                  },
                  "@media (max-width: 600px)": {
                    fontSize: "1rem",
                  },
                  "@media (max-width: 535px)": {
                    fontSize: "1.2rem",
                  },
                }}
              />
            )}
          </Box>
        </Box>
      </MainContent>
    </Container>
  );
};

export default Settings;