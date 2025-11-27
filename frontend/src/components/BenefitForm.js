import React, { useState, useEffect } from 'react';
import {
  TextField,
  Button,
  FormControlLabel,
  Checkbox,
  FormGroup,
  Typography,
  Box,
  Divider,
  Alert
} from '@mui/material';
import { styled } from 'styled-components';
import benefitsService from '../services/benefits.service';
import productsService from '../services/products.service';

const FormContainer = styled.div`
  padding: 1rem;
  max-width: 600px;
  margin: 0 auto;
`;

const SectionTitle = styled(Typography)`
  && {
    margin: 1.5rem 0 1rem 0;
    color: #a4d4cc;
    font-weight: bold;
  }
`;

const SubmitButton = styled(Button)`
  && {
    background-color: #a4d4cc;
    color: white;
    margin-top: 2rem;
    padding: 0.75rem 2rem;
    
    &:hover {
      background-color: #7bb3a8;
    }
  }
`;

const CancelButton = styled(Button)`
  && {
    margin-top: 2rem;
    margin-left: 1rem;
    color: #666;
  }
`;

function BenefitForm({ onSubmit, onCancel }) {
  const [formData, setFormData] = useState({
    type: '',
    pointsRequired: '',
    discountType: '',
    discountValue: '',
    applicableDays: [],
    selectedProductId: ''
  });


  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  
  useEffect(() => {
    loadProductsAndCategories();
  }, []);

  const loadProductsAndCategories = async () => {
    try {
      const productsResponse = await productsService.getAllProducts();
      console.log('Products loaded:', productsResponse.data);
      setProducts(productsResponse.data || []);
    } catch (error) {
      console.error('Error loading data:', error);
    }
  };
  const dayTranslations = {
    'MONDAY': 'Monday',
    'TUESDAY': 'Tuesday',
    'WEDNESDAY': 'Wednesday',
    'THURSDAY': 'Thursday',
    'FRIDAY': 'Friday',
    'SATURDAY': 'Saturday',
    'SUNDAY': 'Sunday'
  };



  const handleChange = (field, value) => {
    console.log('HandleChange called:', field, '=', value, typeof value);
    
    setFormData(prevData => {
      const newData = { ...prevData };
      newData[field] = value;
      

      
      console.log('Previous formData:', prevData);
      console.log('New formData:', newData);
      return newData;
    });
    
    setError('');
  };

  const handleDayToggle = (day) => {
    const updatedDays = formData.applicableDays.includes(day)
      ? formData.applicableDays.filter(d => d !== day)
      : [...formData.applicableDays, day];
    
    handleChange('applicableDays', updatedDays);
  };

  const validateForm = () => {
    if (!formData.type || !formData.pointsRequired) {
      setError('Benefit type and required points are mandatory');
      return false;
    }

    if (formData.pointsRequired <= 0) {
      setError('Required points must be greater than 0');
      return false;
    }

    if (formData.type === 'DISCOUNT') {
      if (!formData.discountType || !formData.discountValue || formData.applicableDays.length === 0) {
        setError('For discounts: type, value and applicable days are required');
        return false;
      }
      if (formData.discountValue <= 0) {
        setError('Discount value must be greater than 0');
        return false;
      }
    }

    if (formData.type === 'FREE_PRODUCT') {
      if (!formData.selectedProductId) {
        setError('You must select a product');
        return false;
      }
      if (formData.applicableDays.length === 0) {
        setError('You must select at least one applicable day');
        return false;
      }
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      const submitData = {
        type: formData.type,
        pointsRequired: parseInt(formData.pointsRequired)
      };
      
      // Only add fields relevant to the benefit type
      if (formData.type === 'DISCOUNT') {
        submitData.discountType = formData.discountType;
        submitData.discountValue = parseFloat(formData.discountValue);
        submitData.applicableDays = formData.applicableDays;
      } else if (formData.type === 'FREE_PRODUCT') {
        submitData.applicableDays = formData.applicableDays;
        submitData.productIds = [parseInt(formData.selectedProductId)];
      }

      await benefitsService.createBenefit(submitData);
      onSubmit();
    } catch (error) {
      console.error('Error creating benefit:', error);
      if (error.response?.status === 403) {
        setError('Access denied. You need ADMIN or MANAGER role to create benefits.');
      } else {
        setError(error.response?.data?.message || 'Error creating benefit');
      }
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      type: '',
      pointsRequired: '',
      discountType: '',
      discountValue: '',
      applicableDays: [],
      selectedProductId: ''
    });
    setError('');
  };

  return (
    <FormContainer>
      <form onSubmit={handleSubmit}>
        {error && (
          <Alert severity="error" style={{ marginBottom: '1rem' }}>
            {error}
          </Alert>
        )}

        {/* Basic Information */}
        <SectionTitle variant="h6">Basic Information</SectionTitle>
        
        <p style={{ color: 'black', fontWeight: 'bold' }}>Benefit Type *</p>
        <select
          value={formData.type || ''}
          onChange={(e) => handleChange('type', e.target.value)}
          style={{ width: '80%', marginTop: '3%', marginBottom: '3%', fontSize: '1.1rem', padding: '8px' }}
        >
          <option value="">Select benefit type</option>
          <option value="DISCOUNT">Discount</option>
          <option value="FREE_PRODUCT">Free Product</option>
        </select>

        <TextField
          fullWidth
          margin="normal"
          label="Required Points"
          type="number"
          value={formData.pointsRequired || ''}
          onChange={(e) => handleChange('pointsRequired', e.target.value)}
          inputProps={{ min: 1 }}
        />

        {/* Discount Configuration */}
        {formData.type === 'DISCOUNT' && (
          <>
            <Divider style={{ margin: '2rem 0 1rem 0' }} />
            <SectionTitle variant="h6">Discount Configuration</SectionTitle>
            
            <p style={{ color: 'black', fontWeight: 'bold' }}>Discount Type *</p>
            <select
              value={formData.discountType || ''}
              onChange={(e) => handleChange('discountType', e.target.value)}
              style={{ width: '80%', marginTop: '3%', marginBottom: '3%', fontSize: '1.1rem', padding: '8px' }}
            >
              <option value="">Select discount type</option>
              <option value="PERCENTAGE">Percentage</option>
              <option value="FIXED_AMOUNT">Fixed Amount</option>
            </select>

            <TextField
              fullWidth
              margin="normal"
              label={formData.discountType === 'PERCENTAGE' ? 'Percentage (%)' : 'Amount ($)'}
              type="number"
              value={formData.discountValue || ''}
              onChange={(e) => handleChange('discountValue', e.target.value)}
              inputProps={{ 
                min: formData.discountType === 'PERCENTAGE' ? 1 : 0.01, 
                step: formData.discountType === 'PERCENTAGE' ? 1 : 0.01,
                max: formData.discountType === 'PERCENTAGE' ? 100 : undefined
              }}
            />

            <Typography variant="subtitle1" style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>
              Applicable Days:
            </Typography>
            <FormGroup row>
              {daysOfWeek.map((day) => (
                <FormControlLabel
                  key={day}
                  control={
                    <Checkbox
                      checked={formData.applicableDays.includes(day)}
                      onChange={() => handleDayToggle(day)}
                    />
                  }
                  label={dayTranslations[day]}
                />
              ))}
            </FormGroup>
          </>
        )}

        {/* Free Product Configuration */}
        {formData.type === 'FREE_PRODUCT' && (
          <>
            <Divider style={{ margin: '2rem 0 1rem 0' }} />
            <SectionTitle variant="h6">Free Product Configuration</SectionTitle>
            
            {/* Product Selector */}
            <Typography variant="subtitle1" style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>
              Select Product:
            </Typography>
                <select
                  value={formData.selectedProductId || ''}
                  onChange={(e) => {
                    console.log('Product selected:', e.target.value);
                    handleChange('selectedProductId', e.target.value);
                  }}
                  style={{ width: '80%', marginBottom: '1rem', padding: '8px', fontSize: '14px' }}
                >
                  <option value="">Select a product</option>
                  {products.map((product) => (
                    <option key={`product-${product.id}`} value={product.id}>
                      {product.name} - ${product.price}
                    </option>
                  ))}
                </select>
            
            {/* Applicable Days */}
            <Typography variant="subtitle1" style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>
              Applicable Days:
            </Typography>
            <FormGroup row>
              {daysOfWeek.map((day) => (
                <FormControlLabel
                  key={day}
                  control={
                    <Checkbox
                      checked={formData.applicableDays.includes(day)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          handleChange('applicableDays', [...formData.applicableDays, day]);
                        } else {
                          handleChange('applicableDays', formData.applicableDays.filter(d => d !== day));
                        }
                      }}
                    />
                  }
                  label={dayTranslations[day]}
                />
              ))}
            </FormGroup>
          </>
        )}

        {/* Form Actions */}
        <Box display="flex" justifyContent="flex-end" style={{ marginTop: '2rem' }}>
          <CancelButton onClick={onCancel}>
            Cancel
          </CancelButton>
          <SubmitButton 
            type="submit" 
            variant="contained"
            disabled={loading}
          >
            {loading ? 'Creating...' : 'Create Benefit'}
          </SubmitButton>
        </Box>
      </form>
    </FormContainer>
  );
}

export default BenefitForm;