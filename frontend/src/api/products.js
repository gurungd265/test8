import api from './index';

const productsApi = {
    getAllProducts: async () => {
            try {
                const response = await api.get('/api/products');
                return response.data;
            } catch (error) {
                console.error(':', error);
                throw error;
            }
        },

        // 特定の商品の物を読み込み
        getProductById: async (productId) => {
            try {
                const response = await api.get(`/api/products/${productId}`);
                return response.data;
            } catch (error) {
                console.error(`商品 ${productId} 読み込み失敗:`, error);
                throw error;
            }
        }
    };


export default productsApi;