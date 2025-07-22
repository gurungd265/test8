import api from './index';

const cartApi = {
    // カートに商品を追加
    addToCart: async (productId, quantity) => {
        try {
            const response = await api.post('/api/cart/add', { productId, quantity });
            return response.data; // 新しく追加された/アップデートされたショッピングカート項目
        } catch (error) {
            console.error('カートに商品を追加できませんでした。', error);
            throw error;
        }
    },

    // ログインしたユーザーのショッピングカート項目の取得
    getCartItems: async () => {
        try {
            const response = await api.get('/api/cart');
            return response.data; // ショッピングカートリスト
        } catch (error) {
            console.error('カートアイテムを読み込むことができませんでした。', error);
            throw error;
        }
    },

    // ショッピングカート項目の数量変更
    updateCartItemQuantity: async (cartItemId, newQuantity) => {
        try {
            const response = await api.put(`/api/cart/${cartItemId}`, { newQuantity });
            return response.data; // アップデートされたショッピングカート項目
        } catch (error) {
            console.error(`カートアイテムID ${cartItemId}の数量を更新できませんでした。`, error);
            throw error;
        }
    },

    // ショッピングカート項目削除
    removeCartItem: async (cartItemId) => {
        try {
            const response = await api.delete(`/api/cart/${cartItemId}`);
            return response.data;
        } catch (error) {
            console.error(`カートアイテムID ${cartItemId}を削除できませんでした。`, error);
            throw error;
        }
    },

    // 買い物かごを空けること
    clearCart: async () => {
        try {
            const response = await api.delete('/api/cart/clear');
            return response.data;
        } catch (error) {
            console.error('カートを空にできませんでした。', error);
            throw error;
        }
    }
};

export default cartApi;