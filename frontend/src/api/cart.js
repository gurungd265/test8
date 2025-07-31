import api from './index';
import Cookies from 'js-cookie';

const isLoggedIn = () => {
    return !!localStorage.getItem('jwtToken');
};

const cartApi = {
    // カートに商品を追加
    addToCart: async (productId, quantity) => {
        let url = `/api/cart/items?productId=${productId}&quantity=${quantity}`;
        if(!isLoggedIn()){
            let sessionId = Cookies.get('sessionId');
            if (!sessionId) {
                sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                Cookies.set('sessionId', sessionId, { expires: 7 , path: '/'});
            }
            url += `&sessionId=${sessionId}`;
        }
        try {
            const response = await api.post(url);
            return response.data;
        } catch (error) {
            console.error('カートに商品を追加できませんでした。', error);
            throw error;
        }
    },

    // ログインしたユーザーのショッピングカート項目の取得
    getCartItems: async () => {
        let url = `/api/cart`;
            if(!isLoggedIn()){
                let sessionId = Cookies.get('sessionId');
                if (!sessionId) {
                    sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                    Cookies.set('sessionId', sessionId, { expires: 7 , path: '/'});
                }
                url += url.includes('?') ? `&sessionId=${sessionId}` : `?sessionId=${sessionId}`;
            }
        try {
            const response = await api.get(url);
            return response.data; // ショッピングカートリスト
        } catch (error) {
            console.error('カートアイテムを読み込むことができませんでした。', error);
            throw error;
        }
    },

    // ショッピングカート項目の数量変更
    updateCartItemQuantity: async (cartItemId, newQuantity) => {
        let url = `/api/cart/items/${cartItemId}?quantity=${newQuantity}`;
                if(!isLoggedIn()){
                    let sessionId = Cookies.get('sessionId');
                    if (!sessionId) {
                        sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                        Cookies.set('sessionId', sessionId, { expires: 7 , path: '/'});
                    }
                    url += `&sessionId=${sessionId}`;
                }
        try {
            const response = await api.put(url);
            return response.data; // アップデートされたショッピングカート項目
        } catch (error) {
            console.error(`カートアイテムID ${cartItemId}の数量を更新できませんでした。`, error);
            throw error;
        }
    },

    // ショッピングカート項目削除
    removeCartItem: async (cartItemId) => {
        let url = `/api/cart/items/${cartItemId}`;
                if(!isLoggedIn()){
                    let sessionId = Cookies.get('sessionId');
                    if (!sessionId) {
                        sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                        Cookies.set('sessionId', sessionId, { expires: 7 , path: '/'});
                    }
                    url += url.includes('?') ? `&sessionId=${sessionId}` : `?sessionId=${sessionId}`;
                }
        try {
            const response = await api.delete(url);
            return response.data;
        } catch (error) {
            console.error(`カートアイテムID ${cartItemId}を削除できませんでした。`, error);
            throw error;
        }
    },

    // 買い物かごを空けること
    clearCart: async () => {
        let url = `/api/cart`;
                if(!isLoggedIn()){
                    let sessionId = Cookies.get('sessionId');
                    if (!sessionId) {
                        sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                        Cookies.set('sessionId', sessionId, { expires: 7 , path: '/'});
                    }
                    url += url.includes('?') ? `&sessionId=${sessionId}` : `?sessionId=${sessionId}`;
                }
        try {
            const response = await api.delete(url);
            return response.data;
        } catch (error) {
            console.error('カートを空にできませんでした。', error);
            throw error;
        }
    },

    mergeAnonymousCart: async (sessionId) => {
            try {
                const response = await api.post(`/api/cart/merge?sessionId=${sessionId}`);
                Cookies.remove('sessionId', { path: '/' });
                return response.data;
            } catch (error) {
                console.error('匿名カートの統合に失敗しました。', error);
                throw error;
            }
    },

    getCartItemCount: async () => {
        let url = '/api/cart/count';
        if (!isLoggedIn()) {
            const sessionId = Cookies.get('sessionId');
            if (sessionId) {
                url += `?sessionId=${sessionId}`;
            }else{
                return 0;
            }
        } else{
            console.log('DEBUG_CART_API: getCartItemCount: not sessionId request:', url);
        }
        try {
            const response = await api.get(url);
            return response.data.items;
        } catch (error) {
            console.error('Failed to fetch cart item count:', error);
            return 0;
        }
    }
};

export default cartApi;