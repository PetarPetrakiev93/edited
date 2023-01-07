SELECT p.id, p.name, COUNT(DISTINCT o.customer_id) as num_customers, COUNT(o.order_id) as num_orders,
       COUNT(o.order_id) / COUNT(DISTINCT o.customer_id) as orders_per_customer,
       AVG(o2.date - o1.date) as avg_days_between_purchases, c.name as most_frequent_customer
FROM products p
JOIN orders o ON o.product_id = p.id
JOIN customers c ON c.customer_id = o.customer_id
LEFT JOIN orders o1 ON o1.customer_id = o.customer_id AND o1.product_id = o.product_id AND o1.date < o.date
LEFT JOIN orders o2 ON o2.customer_id = o1.customer_id AND o2.product_id = o1.product_id AND o2.date > o1.date
GROUP BY p.id, p.name, c.name
HAVING COUNT(DISTINCT o.customer_id) > 1
ORDER BY num_customers DESC, MIN(o.date) ASC, c.customer_id ASC
