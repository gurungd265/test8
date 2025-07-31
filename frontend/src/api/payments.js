import axios from 'axios';

const API_BASE = '/api/payments';

const paymentsApi = {
    createPayment: (paymentRequest) => {
        // paymentRequest = { orderId, paymentMethod, amount, transactionId }
        return axios.post(`${API_BASE}`, paymentRequest)
            .then(res => res.data);
    },

    cancelPayment: (transactionId) => {
        return axios.post(`${API_BASE}/cancel/${transactionId}`)
            .then(res => res.data);
    },

    refundPayment: (transactionId, refundAmount) => {
        return axios.post(`${API_BASE}/refund/${transactionId}`, null, {
            params: { refundAmount }
        }).then(res => res.data);
    },

    getPaymentsByStatus: (status) => {
        return axios.get(`${API_BASE}/status/${status}`)
            .then(res => res.data);
    }
};

export default paymentsApi;