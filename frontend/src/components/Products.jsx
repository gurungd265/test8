import Product from "./Product";
import products from "../data/products.json"

export default function Products() {
  return (
    <>
      {/* Products */}
      <main className="container mx-auto p-4">
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
          {products.map((product) => (
            <Product key={product.id} product={product} />
          ))}
        </section>
      </main>
    </>
  );
}
