package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;

@Repository
public class BoardRepositoryImpl implements BoardRepository {

    private final SpringDataBoardRepository springDataBoardRepository;
    private final BoardMapper boardMapper;

    public BoardRepositoryImpl(SpringDataBoardRepository springDataBoardRepository, BoardMapper boardMapper) {
        this.springDataBoardRepository = springDataBoardRepository;
        this.boardMapper = boardMapper;
    }

    @Override
    public void save(Board board) {
        springDataBoardRepository.save(boardMapper.toEntity(board));
    }

    @Override
    public Optional<Board> findById(BoardId id) {
        return springDataBoardRepository.findById(id.value())
                .map(boardMapper::toModel);
    }

    @Override
    public List<Board> findByEmail(String email) {
        return springDataBoardRepository.findByEmailOrSharedWith(email).stream()
                .map(boardMapper::toModel)
                .toList();
    }
    
    @Override
    public List<Board> findAll() {
        return springDataBoardRepository.findAll()
                .stream()
                .map(boardMapper::toModel)
                .toList();
    }
}