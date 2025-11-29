import "../App.css";
import React, { useState, useEffect, useMemo } from "react";
import styled from "styled-components";
import { useSelector } from "react-redux";
import { Navigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import clientService from "../services/client.service";
import pointsService from "../services/points.service";
import { useTheme } from "@mui/material/styles";
import { Box, Typography, gridClasses, useMediaQuery } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { MdFastfood } from "react-icons/md";
import moment from "moment";
import VisibilityIcon from "@mui/icons-material/Visibility";
import CircularProgress from "@mui/material/CircularProgress";

const Container = styled.div``;

const MainContent = styled.div`
  display: ${(props) => (props.visible ? "" : "none")};
  width: 100%;
  margin: auto;
  padding: 1rem;

  @media (min-width: 768px) {
    width: 80%;
  }
`;

const OrdersTable = styled.div``;

const Details = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  z-index: 9999;
  padding: 1rem;
`;

const DetailsContent = styled.div`
  padding: 2rem;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  max-height: 100vh;
  overflow-y: auto;
  width: 20%;
  margin: auto;
  background-color: black;
  strong {
    color: white;
    font-family: "Roboto";
    font-size: 1.5rem;
  }
  strong{
    color: white;
    font-family: 'Roboto';
    font-size: 1.7rem;
    margin-top: 1rem;
    align-self: center;
    margin-bottom: 1rem;
  }

  @media (max-width: 1500px) {
    width: 30%;
  }
  @media (max-width: 1000px) {
    width: 40%;
  }
  @media (max-width: 800px) {
    width: 100%;
  }
`;

const DetailsButton = styled.button`
  display: block;
  width: 100%;
  font-size: 1.5rem;
  border-radius: 3px;
  padding: 1rem 3.5rem;
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
`;

const MOBILE_COLUMNS = {
  id: true,
  clientId: false,
  dateCreated: true,
  totalPrice: true,
  details: true,
  status: false,
  points: true,
};

const ALL_COLUMNS = {
  id: true,
  username: false,
  clientId: false,
  dateCreated: true,
  totalPrice: true,
  details: true,
  status: false,
  points: true,
};

function MyOrders() {
  const { user: currentUser } = useSelector((state) => state.auth);
  const clicked = useSelector((state) => state.navigation.clicked);

  const theme = useTheme();
  const matches = useMediaQuery(theme.breakpoints.up("sm"));

  const [orders, setOrders] = useState([]);
  const [totalPoints, setTotalPoints] = useState("0.00");
  const [conversionRate, setConversionRate] = useState(100);
  const [isLoading, setIsLoading] = useState(true);

  const [columnVisible, setColumnVisible] = useState(ALL_COLUMNS);
  const [isDetailsVisible, setIsDetailsVisible] = useState(false);
  const [selectedOrderDetails, setSelectedOrderDetails] = useState([]);
  const [selectedTotalPrice, setSelectedTotalPrice] = useState(null);

  useEffect(() => {
    const newColumns = matches ? ALL_COLUMNS : MOBILE_COLUMNS;
    setColumnVisible(newColumns);
  }, [matches]);

  useEffect(() => {
    // Load orders
    clientService
      .getClientOrders()
      .then((response) => {
        setOrders(response.data);
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("Error al mostrar las ordenes", error);
        setIsLoading(false);
      });

    // Load points from new API
    pointsService
      .getCurrentPoints()
      .then((response) => {
        setTotalPoints(response.data.currentBalance.toFixed(2));
      })
      .catch((error) => {
        console.error("Error al cargar puntos", error);
        setTotalPoints("0.00");
      });

    // Load current conversion rate
    fetch("http://localhost:8080/settings/points-conversion", {
      headers: {
        "Authorization": "Bearer " + currentUser.access_token,
        "Content-Type": "application/json"
      }
    })
    .then(response => response.json())
    .then(data => {
      setConversionRate(data.conversionRate);
    })
    .catch(error => {
      console.error("Error al cargar tasa de conversión", error);
    });
  }, [currentUser.access_token]);

  if (!currentUser || currentUser.role !== "CLIENT") {
    return <Navigate to="/login" />;
  }

  const contentVisible = !clicked;

  const handleViewDetails = (orderId) => {
    const selectedOrder = orders.find((order) => order.id === orderId);
    setSelectedOrderDetails(selectedOrder.productOrders);
    setSelectedTotalPrice(selectedOrder.totalPrice);
    setIsDetailsVisible(true);
  };

  const handleCloseDetails = () => {
    setIsDetailsVisible(false);
  };

  const rows = orders.map((order) => {
    const isClosed = order.status && order.status.toUpperCase() === "CLOSED";
    let points = "0.00";
    
    if (isClosed && order.clientId) {
      if (order.pointsAwarded) {
        points = order.pointsAwarded.toFixed(2);
      } else {
        // Órdenes históricas: calcular con tasa de conversión actual
        points = (order.totalPrice / conversionRate).toFixed(2);
      }
    }
    
    // Formatear nombre del beneficio
    let benefitName = null;
    if (order.appliedBenefit) {
      const benefit = order.appliedBenefit;
      if (benefit.type === "DISCOUNT") {
        if (benefit.discountType === "PERCENTAGE") {
          benefitName = `${benefit.discountValue}% OFF`;
        } else {
          benefitName = `$${benefit.discountValue} OFF`;
        }
      } else if (benefit.type === "FREE_PRODUCT") {
        // Para productos gratis, idealmente mostrar el nombre del producto
        benefitName = `Free Product`;
      }
    }
    
    return {
      id: order.id,
      username: order.user.username,
      clientId: order.clientId || "N/A",
      dateCreated: order.dateCreated,
      totalPrice: order.totalPrice.toLocaleString("en-US", {
        style: "currency",
        currency: "USD",
      }),
      status: order.status,
      points: points,
      pointsUsed: (order.pointsUsed || 0).toFixed(2),
      benefitName: benefitName,
      productOrders: order.productOrders,
    };
  });

  const columns = [
    {
      field: "id",
      headerName: "ID",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: false,
      minWidth: 90,
    },
    {
      field: "username",
      headerName: "Vendor",
      flex: 2,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 150,
    },
    {
      field: "clientId",
      headerName: "Client",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 100,
      renderCell: (params) => (
        params.row.clientId ? params.row.clientId : "N/A"
      ),
    },
    {
      field: "dateCreated",
      headerName: "Date",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 150,
      renderCell: (params) =>
        moment(params.row.dateCreated).format("YYYY-MM-DD HH:MM:SS"),
    },
    {
      field: "totalPrice",
      headerName: "Total",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 150,
    },
    {
      field: "details",
      headerName: "Products",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: false,
      renderCell: (params) => (
        <VisibilityIcon onClick={() => handleViewDetails(params.row.id)} />
      ),
    },
    {
      field: "status",
      headerName: "Status",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 150,
    },
    {
      field: "points",
      headerName: "Points Earned",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 120,
      renderCell: (params) => {
        return (
          <Typography variant="body2" sx={{ fontWeight: 'bold', color: '#4caf50' }}>
            +{params.value}
          </Typography>
        );
      },
    },
    {
      field: "pointsUsed",
      headerName: "Points Used",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 120,
      renderCell: (params) => {
        return (
          <Typography variant="body2" sx={{ fontWeight: 'bold', color: '#f44336' }}>
            -{params.value || '0.00'}
          </Typography>
        );
      },
    },
    {
      field: "benefitName",
      headerName: "Benefit",
      flex: 1,
      align: "center",
      headerAlign: "center",
      sortable: true,
      minWidth: 150,
      renderCell: (params) => (
        params.row.benefitName || "N/A"
      ),
    },
  ];

  return (
    <Container>
      <Navbar />
      <MainContent visible={contentVisible}>
        <div>
          {isLoading ? (
            <Box
              sx={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                marginTop: "20%",
              }}
            >
              <CircularProgress style={{ color: "#a4d4cc" }} />
            </Box>
          ) : (
            <OrdersTable sx={{ width: "100%", backgroundColor: "blue" }}>
              <Box sx={{ height: 400, width: "100%" }}>
                <Typography
                  variant="h3"
                  component="h3"
                  sx={{ textAlign: "center", mt: 3, mb: 1, color: "white" }}
                >
                  My Orders
                </Typography>
                <Typography
                  variant="h5"
                  component="h5"
                  sx={{ textAlign: "center", mb: 1, color: "#a4d4cc", fontWeight: "bold" }}
                >
                  Total Points: {totalPoints}
                </Typography>
                <Typography
                  variant="body1"
                  component="p"
                  sx={{ textAlign: "center", mb: 3, color: "white", fontSize: "0.9rem" }}
                >
                  Current conversion: ${conversionRate} = 1 point
                </Typography>
                <DataGrid
                  initialState={{
                    pagination: { paginationModel: { pageSize: 5 } },
                    sorting: {
                      sortModel: [{ field: "dateCreated", sort: "desc" }],
                    },
                  }}
                  autoHeight={true}
                  columns={columns}
                  columnVisibilityModel={columnVisible}
                  rows={rows}
                  getRowId={(row) => row.id}
                  pageSizeOptions={[5, 10, 25]}
                  pagination
                  getRowSpacing={(params) => ({
                    top: params.isFirstVisible ? 0 : 5,
                    bottom: params.isLastVisible ? 0 : 5,
                  })}
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
                    "@media (max-width: 535px)": {
                      fontSize: "1.2rem",
                    },
                  }}
                />
              </Box>
            </OrdersTable>
          )}

          {isDetailsVisible && (
            <Details>
              <DetailsContent>
                {selectedOrderDetails.map((productOrder) => (
                  <div key={productOrder.productOrderId}>
                    <strong>
                      {productOrder.quantity}x {productOrder.productName}
                    </strong>
                    <br />
                    <strong>${productOrder.productUnitPrice} ea.</strong>
                    <br />
                    <strong></strong>
                    <br />
                  </div>
                ))}
                <strong style={{ color: "white" }}>Total price: ${selectedTotalPrice}</strong>
                <DetailsButton onClick={() => handleCloseDetails()}>
                  Close
                </DetailsButton>
              </DetailsContent>
            </Details>
          )}
        </div>
      </MainContent>
    </Container>
  );
}

export default MyOrders;