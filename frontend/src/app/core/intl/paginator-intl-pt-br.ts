import { MatPaginatorIntl } from '@angular/material/paginator';

/** Traduz os textos do MatPaginator para português (pt-BR). */
export function criarPaginatorIntlPtBr(): MatPaginatorIntl {
  const intl = new MatPaginatorIntl();

  intl.itemsPerPageLabel = 'Itens por página:';
  intl.nextPageLabel = 'Próxima página';
  intl.previousPageLabel = 'Página anterior';
  intl.firstPageLabel = 'Primeira página';
  intl.lastPageLabel = 'Última página';

  intl.getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0 || pageSize === 0) {
      return `0 de ${length}`;
    }
    const totalPaginas = Math.max(length, 0);
    const inicio = page * pageSize;
    const fim = inicio < totalPaginas ? Math.min(inicio + pageSize, totalPaginas) : inicio + pageSize;
    return `${inicio + 1} – ${fim} de ${totalPaginas}`;
  };

  return intl;
}
