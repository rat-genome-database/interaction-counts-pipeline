# interaction-counts-pipeline

Computes interaction counts for genes and proteins and stores them in the INTERACTION_COUNTS table. Only non-zero interaction counts are stored.

## What it does

1. Loads all active genes and proteins from RGD.
2. For each gene, counts gene-protein interactions using parallel processing.
3. For each protein, counts protein-protein interactions using parallel processing.
4. Inserts new counts or updates existing ones in the INTERACTION_COUNTS table.
5. Deletes any entries where the interaction count has dropped to zero.

## Output

- `interaction_counts` table rows with: RGD ID and interaction count
- Pipeline logs with statistics: records up-to-date, updated, inserted, and zero-count deletions
