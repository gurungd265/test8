import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import Product from '../components/Product.jsx';
import CategoryFilter from '../components/CategoryFilter';
import Pagination from '../components/Pagination';

export default function FilteredProductPage() {

    const [searchParams] = useSearchParams();

    const categoryIdFromUrl = searchParams.get('categoryId');
    const keywordFromUrl = searchParams.get('keyword') || '';
    const pageFromUrl = parseInt(searchParams.get('page')) || 1;

    const [page, setPage] = useState(pageFromUrl);

    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [totalPages, setTotalPages] = useState(1);
    const [totalProducts, setTotalProducts] = useState(0);
    const navigate = useNavigate();

    const fetchProducts = async (categoryId = null, pageNum = 1) => {
        try {
            setLoading(true);
            setError(null);
            let url = `http://localhost:8080/api/products?page=${pageNum}`;
            if (categoryId) {
                url += `&categoryId=${categoryId}`;
            }
            console.log('Fetching:', url); // 여기 추가

            const response = await axios.get(url);
            const data = response.data;

            setProducts(data.content || data);
            setTotalPages(data.totalPages || 1);
            setTotalProducts(data.totalElements || data.length);
        } catch (err) {
            console.error('Error fetching products:', err);
            setError('Failed to load products. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/categories');
            setCategories(response.data);
        } catch (err) {
            console.error('Error fetching categories:', err);
            setError('Failed to load categories. Product filtering may not work.');
        }
    };

    const fetchSearchProducts = async (keyword, pageNum = 1) => {
        try {
            setLoading(true);
            setError(null);

            const response = await axios.get('http://localhost:8080/api/products/search', {
                params: { keyword, page: pageNum }
            });

            const data = response.data;
            setProducts(data.content || []);
            setTotalPages(data.totalPages || 1);
            setTotalProducts(data.totalElements || data.length);
            setPage(pageNum);
        } catch (err) {
            console.error('Search error:', err);
            setError('Failed to load product.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        setPage(pageFromUrl); // URL 쿼리가 바뀌면 페이지도 변경
    }, [pageFromUrl]);

    // 1) 카테고리 리스트는 처음 한번만 불러오기
    useEffect(() => {
        fetchCategories();
    }, []);

    // 2) URL 카테고리 혹은 검색 키워드가 바뀔 때마다 상품 데이터 다시 불러오기
    useEffect(() => {
        if (keywordFromUrl.trim()) {
            fetchSearchProducts(keywordFromUrl, page);
        } else {
            setSelectedCategory(categoryIdFromUrl || null);
            fetchProducts(categoryIdFromUrl, page);
        }
    }, [categoryIdFromUrl, keywordFromUrl, page]);

    const handleFilter = (categoryId) => {
        setSelectedCategory(categoryId);
        // 카테고리 변경 시 URL 변경 (page도 1로 초기화)
        navigate(`/products?categoryId=${categoryId || ''}&page=1`);
    };

    const handlePageChange = (newPage) => {
        if (newPage > 0 && newPage <= totalPages) {
            setPage(newPage);
            // URL에 page 쿼리 추가 (keyword, categoryId 유지)
            const params = new URLSearchParams(searchParams);
            params.set('page', newPage);
            navigate(`/products?${params.toString()}`);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    };

    if (error) {
        return <div className="p-4 text-red-600">{error}</div>;
    }

    if (loading && products.length === 0) {
        return <div className="p-4 text-gray-600">Loading products...</div>;
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex flex-col md:flex-row gap-8">
                {/* Filters Sidebar */}
                <div className="w-full md:w-64">
                    <CategoryFilter
                        categories={categories}
                        selectedCategory={selectedCategory}
                        onFilter={handleFilter}
                    />
                </div>

                {/* Products Grid */}
                <div className="flex-1">
                    <h1 className="text-2xl font-bold mb-6">
                        Products {totalProducts > 0 && `(${totalProducts})`}
                    </h1>

                    {loading && products.length > 0 && (
                        <div className="text-gray-600 mb-4">Loading more products...</div>
                    )}

                    {products.length === 0 && !loading ? (
                        <div className="text-gray-600 text-center py-12">
                            No products found. Try adjusting your filters.
                        </div>
                    ) : (
                        <>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                                {products.map((product) => (
                                    <Product key={product.id} product={product} />
                                ))}
                            </div>

                            {totalPages > 1 && (
                                <Pagination
                                    currentPage={page}
                                    totalPages={totalPages}
                                    onPageChange={handlePageChange}
                                />
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}